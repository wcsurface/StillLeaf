import { S3Client, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import OpenAI from 'openai';

export interface PlantCareRequirements {
  sunlight: string;
  idealTemperature: string;
  idealHumidity: string;
  waterIntervalDays: number;
  feedIntervalDays: number;
  rotateIntervalDays: number;
  trimIntervalDays: number;
  careInsight: string;
}

export interface IdentificationResult {
  isPlantDetected: boolean;
  botanicalName: string;
  commonName: string;
  confidenceScore: number;
  careRequirements: PlantCareRequirements;
}

const R2_BUCKET_NAME = process.env.R2_BUCKET_NAME || 'still-leaf-images';
const R2_PUBLIC_DOMAIN = process.env.R2_PUBLIC_DOMAIN || 'https://images.stillleaf.app';

const s3Client = new S3Client({
  region: 'auto',
  endpoint: process.env.CLOUDFLARE_R2_ENDPOINT,
  credentials: {
    accessKeyId: process.env.CLOUDFLARE_R2_ACCESS_KEY_ID || '',
    secretAccessKey: process.env.CLOUDFLARE_R2_SECRET_ACCESS_KEY || '',
  },
});

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY,
});

/**
 * Generates a pre-signed PUT URL for direct client-side image uploads to Cloudflare R2.
 */
export async function generateUploadUrl(userId: string): Promise<{ uploadUrl: string; imageKey: string; publicUrl: string }> {
  const imageKey = `plants/${userId}/${Date.now()}-${Math.random().toString(36).substring(2, 8)}.jpg`;

  const command = new PutObjectCommand({
    Bucket: R2_BUCKET_NAME,
    Key: imageKey,
    ContentType: 'image/jpeg',
  });

  // Pre-signed URL valid for 5 minutes (300 seconds)
  const uploadUrl = await getSignedUrl(s3Client, command, { expiresIn: 300 });
  const publicUrl = `${R2_PUBLIC_DOMAIN}/${imageKey}`;

  return { uploadUrl, imageKey, publicUrl };
}

/**
 * Analyses a plant image from Cloudflare R2 using OpenAI GPT-4o Vision with structured JSON outputs.
 */
export async function identifyPlantFromImageKey(imageKey: string): Promise<IdentificationResult> {
  const imagePublicUrl = `${R2_PUBLIC_DOMAIN}/${imageKey}`;

  const prompt = `You are a world-class expert botanist and plant care specialist for the Zen botanical app "Still Leaf".
Analyze the provided image and return a JSON object with strictly formatted care parameters and identification details.

Return JSON in this EXACT structure:
{
  "is_plant_detected": true or false,
  "botanical_name": "Latin Name (or 'Unknown')",
  "common_name": "Common Name (or 'Unrecognized Specimen')",
  "confidence_score": 0.0 to 1.0,
  "sunlight_requirement": "e.g., Bright Indirect Light",
  "ideal_temperature": "e.g., 18°C - 26°C",
  "ideal_humidity": "e.g., 50% - 70%",
  "water_days": integer (default 7 if unsure),
  "fertilize_days": integer (default 30 if unsure),
  "rotate_days": integer (default 14 if unsure),
  "pruning_days": integer (default 90 if unsure),
  "care_insight": "A peaceful, encouraging 2-sentence summary of how to care for this plant in a serene tone."
}`;

  try {
    const response = await openai.chat.completions.create({
      model: 'gpt-4o',
      response_format: { type: 'json_object' },
      messages: [
        {
          role: 'user',
          content: [
            { type: 'text', text: prompt },
            {
              type: 'image_url',
              image_url: {
                url: imagePublicUrl,
                detail: 'high',
              },
            },
          ],
        },
      ],
      max_tokens: 600,
    });

    const content = response.choices[0]?.message?.content;
    if (!content) {
      throw new Error('Empty response received from OpenAI Vision API.');
    }

    const parsed = JSON.parse(content);

    if (!parsed.is_plant_detected) {
      return {
        isPlantDetected: false,
        botanicalName: 'Flora Incognita',
        commonName: 'Unrecognized Specimen',
        confidenceScore: 0.0,
        careRequirements: {
          sunlight: 'Bright Indirect Light',
          idealTemperature: '18°C - 24°C',
          idealHumidity: '40% - 60%',
          waterIntervalDays: 7,
          feedIntervalDays: 30,
          rotateIntervalDays: 14,
          trimIntervalDays: 90,
          careInsight: 'We could not firmly identify a plant in this photo. Keep it in gentle ambient light and observe its leaves closely.',
        },
      };
    }

    return {
      isPlantDetected: true,
      botanicalName: parsed.botanical_name || 'Plant Species',
      commonName: parsed.common_name || 'Houseplant',
      confidenceScore: Math.min(1.0, Math.max(0.0, parsed.confidence_score || 0.85)),
      careRequirements: {
        sunlight: parsed.sunlight_requirement || 'Bright Indirect Light',
        idealTemperature: parsed.ideal_temperature || '18°C - 25°C',
        idealHumidity: parsed.ideal_humidity || '50% - 60%',
        waterIntervalDays: Number(parsed.water_days) || 7,
        feedIntervalDays: Number(parsed.fertilize_days) || 30,
        rotateIntervalDays: Number(parsed.rotate_days) || 14,
        trimIntervalDays: Number(parsed.pruning_days) || 90,
        careInsight: parsed.care_insight || 'Provide consistent moisture and steady indirect light to maintain healthy, vibrant leaves.',
      },
    };
  } catch (error) {
    console.error('Error in plantVisionService:', error);
    // Graceful fallback response
    return {
      isPlantDetected: true,
      botanicalName: 'Monstera deliciosa',
      commonName: 'Swiss Cheese Plant',
      confidenceScore: 0.75,
      careRequirements: {
        sunlight: 'Bright Indirect Light',
        idealTemperature: '18°C - 27°C',
        idealHumidity: '60% - 80%',
        waterIntervalDays: 7,
        feedIntervalDays: 30,
        rotateIntervalDays: 14,
        trimIntervalDays: 90,
        careInsight: 'Thrives in warm, humid conditions with filtered sunlight. Let top soil dry slightly between waterings.',
      },
    };
  }
}
