import { S3Client, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';

const R2_ACCOUNT_ID = process.env.R2_ACCOUNT_ID || process.env.CLOUDFLARE_R2_ACCOUNT_ID || '';
const R2_ACCESS_KEY_ID = process.env.R2_ACCESS_KEY_ID || process.env.CLOUDFLARE_R2_ACCESS_KEY_ID || '';
const R2_SECRET_ACCESS_KEY = process.env.R2_SECRET_ACCESS_KEY || process.env.CLOUDFLARE_R2_SECRET_ACCESS_KEY || '';
const R2_BUCKET_NAME = process.env.R2_BUCKET_NAME || 'still-leaf-plants';
const R2_PUBLIC_DOMAIN = process.env.R2_PUBLIC_DOMAIN || `https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com/${R2_BUCKET_NAME}`;

const s3Client = new S3Client({
  region: 'auto',
  endpoint: `https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com`,
  credentials: {
    accessKeyId: R2_ACCESS_KEY_ID,
    secretAccessKey: R2_SECRET_ACCESS_KEY,
  },
});

export interface PreSignedUploadResult {
  uploadUrl: string;
  imageKey: string;
  publicUrl: string;
}

/**
 * Generates a pre-signed PUT URL for direct client-side upload to Cloudflare R2
 */
export async function generatePreSignedUploadUrl(
  userId: String,
  contentType: string = 'image/jpeg'
): Promise<PreSignedUploadResult> {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 10000);
  const imageKey = `users/${userId}/plants/${timestamp}_${random}.jpg`;

  const command = new PutObjectCommand({
    Bucket: R2_BUCKET_NAME,
    Key: imageKey,
    ContentType: contentType,
  });

  const uploadUrl = await getSignedUrl(s3Client, command, { expiresIn: 3600 });
  const publicUrl = `${R2_PUBLIC_DOMAIN}/${imageKey}`;

  return { uploadUrl, imageKey, publicUrl };
}

/**
 * Generates a temporary pre-signed GET URL for reading protected R2 assets
 */
export async function generatePreSignedReadUrl(imageKey: string): Promise<string> {
  if (imageKey.startsWith('http')) {
    return imageKey;
  }

  const command = new GetObjectCommand({
    Bucket: R2_BUCKET_NAME,
    Key: imageKey,
  });

  return getSignedUrl(s3Client, command, { expiresIn: 1800 });
}
