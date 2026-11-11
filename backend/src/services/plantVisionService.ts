import { PrismaClient, SubscriptionTier } from '@prisma/client';
import OpenAI from 'openai';
import { generatePreSignedReadUrl } from './r2Service';

const prisma = new PrismaClient();
const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY || '',
});

export interface PlantIdentificationResponse {
  botanicalName: string;
  commonName: string;
  confidenceScore: number;
  sunlightRequirement: string;
  idealTemperature: string;
  idealHumidity: string;
  waterIntervalDays: number;
  feedIntervalDays: number;
  rotateIntervalDays: number;
  trimIntervalDays: number;
  soilCategory: string;
  zenSummary: string;
  recommendedAffiliate?: {
    title: string;
    brand: string;
    recommendationNote: string;
    affiliateUrl: string;
    imageUrl?: string | null;
  } | null;
  freeScansRemaining: number;
}

export async function identifyPlantFromDualBase64(
  base64CloseUp?: string,
  base64Whole?: string
) {
  const promptText = `
    You are a master botanist for Still Leaf - Whole Plant Care. Analyze BOTH photos of this plant:
    - Photo 1 provides a close-up detail shot of the leaf margins, veins, or flower bloom.
    - Photo 2 provides a whole plant overview shot in its pot.

    Combine leaf details from Photo 1 and growth habit from Photo 2 to distinguish subtle plant species differences.
    Return a strict JSON object with these exact keys:
    {
      "is_plant_detected": true,
      "botanical_name": "Latin Name",
      "common_name": "Common Name",
      "confidence_score": 98,
      "sunlight_requirement": "e.g. Full Direct Sunlight or Bright Indirect Light",
      "water_days": 7,
      "fertilize_days": 30,
      "rotate_days": 14,
      "trim_days": 90,
      "care_insight": "A 2-sentence care guidance note tailored for this specific species."
    }
  `;

  const contentArray: any[] = [{ type: 'text', text: promptText }];

  if (base64CloseUp) {
    contentArray.push({
      type: 'image_url',
      image_url: { url: `data:image/jpeg;base64,${base64CloseUp}`, detail: 'high' }
    });
  }

  if (base64Whole) {
    contentArray.push({
      type: 'image_url',
      image_url: { url: `data:image/jpeg;base64,${base64Whole}`, detail: 'high' }
    });
  }

  let completion;
  try {
    completion = await openai.chat.completions.create({
      model: 'chatgpt-4o-latest',
      response_format: { type: 'json_object' },
      max_tokens: 700,
      messages: [{ role: 'user', content: contentArray }]
    });
  } catch (_: any) {
    completion = await openai.chat.completions.create({
      model: 'gpt-4o-mini',
      response_format: { type: 'json_object' },
      max_tokens: 700,
      messages: [{ role: 'user', content: contentArray }]
    });
  }

  const rawJson = completion.choices[0]?.message?.content || '{}';
  return JSON.parse(rawJson);
}

export async function identifyPlantFromR2(
  userId: string,
  imageKey: string
): Promise<PlantIdentificationResponse> {
  // 1. Check User & Subscription Tier Quota
  const user = await prisma.user.findUnique({
    where: { id: userId },
  });

  if (!user) {
    throw new Error('USER_NOT_FOUND');
  }

  const isPro = user.tier !== SubscriptionTier.FREE;

  if (!isPro && user.freeScansRemaining <= 0) {
    const error = new Error('QUOTA_EXCEEDED');
    (error as any).statusCode = 403;
    throw error;
  }

  // 2. Obtain Read URL from Cloudflare R2
  const imageUrl = await generatePreSignedReadUrl(imageKey);

  // 3. Call OpenAI GPT-4o Vision with Structured JSON Output
  const promptText = `
    You are a master botanist for Still Leaf - Whole Plant Care.
    Analyze this plant photo and return a JSON object following this exact schema:
    {
      "botanical_name": "Latin Botanical Name",
      "common_name": "Common Plant Name",
      "confidence_score": 0.95,
      "sunlight_requirement": "Bright Indirect Light",
      "ideal_temperature": "18°C - 27°C",
      "ideal_humidity": "50% - 70%",
      "water_days": 7,
      "fertilize_days": 30,
      "rotate_days": 14,
      "pruning_days": 60,
      "soil_category": "SOIL_AROID",
      "zen_summary": "Peaceful 2-sentence guide on nourishing this plant in your home."
    }
  `;

  const completion = await openai.chat.completions.create({
    model: 'gpt-4o-mini',
    response_format: { type: 'json_object' },
    max_tokens: 600,
    messages: [
      {
        role: 'user',
        content: [
          { type: 'text', text: promptText },
          { type: 'image_url', image_url: { url: imageUrl, detail: 'high' } },
        ],
      },
    ],
  });

  const rawJson = completion.choices[0]?.message?.content || '{}';
  const parsed = JSON.parse(rawJson);

  const soilCategory = parsed.soil_category || 'SOIL_AROID';

  // 4. Fetch Matching Affiliate Product
  const affiliate = await prisma.affiliateProduct.findFirst({
    where: {
      category: soilCategory,
      isActive: true,
    },
  });

  // 5. Decrement Free Scan Quota if FREE tier
  let remainingScans = user.freeScansRemaining;
  if (!isPro) {
    remainingScans = Math.max(0, user.freeScansRemaining - 1);
    await prisma.user.update({
      where: { id: userId },
      data: { freeScansRemaining: remainingScans },
    });
  }

  return {
    botanicalName: parsed.botanical_name || 'Flora Incognita',
    commonName: parsed.common_name || 'Sanctuary Plant',
    confidenceScore: parsed.confidence_score || 0.92,
    sunlightRequirement: parsed.sunlight_requirement || 'Bright Indirect Light',
    idealTemperature: parsed.ideal_temperature || '18°C - 27°C',
    idealHumidity: parsed.ideal_humidity || '50% - 70%',
    waterIntervalDays: parsed.water_days || 7,
    feedIntervalDays: parsed.fertilize_days || 30,
    rotateIntervalDays: parsed.rotate_days || 14,
    trimIntervalDays: parsed.pruning_days || 60,
    soilCategory: soilCategory,
    zenSummary: parsed.zen_summary || 'Nourish with mindful watering and soft ambient sunlight.',
    recommendedAffiliate: affiliate
      ? {
          title: affiliate.title,
          brand: affiliate.brand,
          recommendationNote: affiliate.recommendationNote,
          affiliateUrl: affiliate.affiliateUrl,
          imageUrl: affiliate.imageUrl,
        }
      : null,
    freeScansRemaining: isPro ? 999999 : remainingScans,
  };
}

export const identifyPlantFromImageKey = identifyPlantFromR2;
