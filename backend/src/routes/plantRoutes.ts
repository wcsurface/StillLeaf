import { Router, Request, Response } from 'express';
import { generateUploadUrl, identifyPlantFromImageKey } from '../services/plantVisionService';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

/**
 * POST /api/plants/upload-url
 * Generates a pre-signed PUT URL for uploading plant photos directly to Cloudflare R2.
 */
router.post('/upload-url', async (req: Request, res: Response) => {
  try {
    const userId = (req.body?.userId as string) || 'demo-user-123';
    const result = await generateUploadUrl(userId);
    return res.status(200).json({ success: true, data: result });
  } catch (error) {
    console.error('Error generating upload URL:', error);
    return res.status(500).json({ success: false, error: 'Failed to generate upload URL' });
  }
});

/**
 * POST /api/plants/identify
 * Triggers OpenAI GPT-4o Vision analysis on uploaded image key and saves the plant profile.
 */
router.post('/identify', async (req: Request, res: Response) => {
  try {
    const { imageKey, nickname, userId } = req.body;
    if (!imageKey) {
      return res.status(400).json({ success: false, error: 'imageKey is required' });
    }

    const currentUserId = userId || 'demo-user-123';

    // Verify user plant count limit if Free tier
    const user = await prisma.user.findUnique({ where: { id: currentUserId } });
    if (user && user.subscriptionStatus === 'FREE') {
      const plantCount = await prisma.plant.count({ where: { userId: currentUserId } });
      if (plantCount >= 2) {
        return res.status(403).json({
          success: false,
          error: 'Free tier maximum plant limit reached (2 plants max). Please upgrade to Pro for unlimited plants.'
        });
      }
    }

    // Call OpenAI Vision Service
    const visionResult = await identifyPlantFromImageKey(imageKey);

    const publicPhotoUrl = `https://${process.env.R2_BUCKET_NAME || 'still-leaf-images'}.r2.cloudflarestorage.com/${imageKey}`;

    // Create plant in PostgreSQL via Prisma
    const newPlant = await prisma.plant.create({
      data: {
        userId: currentUserId,
        nickname: nickname || visionResult.commonName,
        botanicalName: visionResult.botanicalName,
        commonName: visionResult.commonName,
        careRequirements: visionResult.careRequirements as any,
        photos: {
          create: {
            photoUrl: publicPhotoUrl,
            isQuarterlyMilestone: true,
            note: 'Day 0 - Initial AI Botanical Identification'
          }
        }
      },
      include: {
        photos: true
      }
    });

    return res.status(201).json({
      success: true,
      message: 'Plant identified and added successfully',
      data: newPlant,
      confidenceScore: visionResult.confidenceScore
    });
  } catch (error) {
    console.error('Error identifying plant:', error);
    return res.status(500).json({ success: false, error: 'Plant identification failed' });
  }
});

/**
 * GET /api/plants
 * Fetches all plants for a user.
 */
router.get('/', async (req: Request, res: Response) => {
  try {
    const userId = (req.query.userId as string) || 'demo-user-123';
    const plants = await prisma.plant.findMany({
      where: { userId },
      include: {
        photos: { orderBy: { takenAt: 'asc' } },
        careLogs: { orderBy: { completedAt: 'desc' }, take: 10 }
      }
    });
    return res.status(200).json({ success: true, data: plants });
  } catch (error) {
    console.error('Error fetching plants:', error);
    return res.status(500).json({ success: false, error: 'Failed to fetch plants' });
  }
});

export default router;
