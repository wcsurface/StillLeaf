import { Router, Request, Response } from 'express';
import { identifyPlantFromImageKey, identifyPlantFromDualBase64 } from '../services/plantVisionService';
import { generateUploadUrl } from '../services/r2Service';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

/**
 * Dual-photo vision analysis handler
 */
const handleDualVision = async (req: Request, res: Response) => {
  try {
    const { base64CloseUp, base64Whole } = req.body;
    if (!base64CloseUp && !base64Whole) {
      return res.status(400).json({ success: false, error: 'At least one base64 photo is required' });
    }

    const visionResult = await identifyPlantFromDualBase64(base64CloseUp, base64Whole);
    return res.status(200).json({
      success: true,
      message: 'Dual-photo botanical vision analysis complete',
      data: visionResult
    });
  } catch (error: any) {
    console.error('Error identifying plant via dual base64:', error);
    return res.status(500).json({ success: false, error: error?.message || 'Plant identification failed' });
  }
};

router.post('/identify-dual', handleDualVision);
router.post('/identify-base64', handleDualVision);

/**
 * POST /api/plants/sync
 * Auto-upserts User and backs up Plant profile to Railway PostgreSQL Database safely.
 */
router.post('/sync', async (req: Request, res: Response) => {
  try {
    const { userId, email, plant } = req.body;
    if (!userId && !email) {
      return res.status(400).json({ success: false, error: 'userId or email is required' });
    }

    const userEmail = (email || '').trim().toLowerCase() || `${userId}@stillleaf.app`;

    // 1. Ensure User exists in PostgreSQL with upsert
    const user = await prisma.user.upsert({
      where: { email: userEmail },
      update: { updatedAt: new Date() },
      create: {
        id: userId || `usr_${userEmail.replace(/[^a-z0-9]/g, '_')}`,
        email: userEmail,
        displayName: userEmail.split('@')[0],
        tier: 'LIFETIME'
      }
    });

    if (!plant || !plant.id) {
      return res.status(200).json({ success: true, message: 'User synchronized successfully', user });
    }

    const careReq = plant.careRequirements || {};

    // 2. Upsert Plant in PostgreSQL
    const syncedPlant = await prisma.plant.upsert({
      where: { id: plant.id },
      update: {
        nickname: plant.nickname || 'My Plant',
        botanicalName: plant.botanicalName || '',
        commonName: plant.commonName || '',
        sunlightRequirement: careReq.sunlight || 'Bright Indirect Light',
        waterIntervalDays: careReq.waterIntervalDays || 7,
        feedIntervalDays: careReq.feedIntervalDays || 30,
        rotateIntervalDays: careReq.rotateIntervalDays || 14,
        trimIntervalDays: careReq.trimIntervalDays || 60,
        careSummary: careReq.careInsight || '',
        updatedAt: new Date()
      },
      create: {
        id: plant.id,
        userId: user.id,
        nickname: plant.nickname || 'My Plant',
        botanicalName: plant.botanicalName || '',
        commonName: plant.commonName || '',
        sunlightRequirement: careReq.sunlight || 'Bright Indirect Light',
        waterIntervalDays: careReq.waterIntervalDays || 7,
        feedIntervalDays: careReq.feedIntervalDays || 30,
        rotateIntervalDays: careReq.rotateIntervalDays || 14,
        trimIntervalDays: careReq.trimIntervalDays || 60,
        careSummary: careReq.careInsight || ''
      }
    });

    // 3. Upsert primary photo if available
    if (plant.primaryPhotoUrl) {
      await prisma.plantPhoto.create({
        data: {
          plantId: syncedPlant.id,
          photoUrl: plant.primaryPhotoUrl,
          isQuarterlyMilestone: true,
          note: 'Day 0 - Sanctuary Plant Profile Photo'
        }
      }).catch(() => {});
    }

    return res.status(200).json({
      success: true,
      message: 'Plant backed up to Railway PostgreSQL successfully',
      data: syncedPlant
    });
  } catch (error: any) {
    console.error('Error syncing plant to Railway PostgreSQL:', error);
    return res.status(500).json({ success: false, error: error?.message || 'Plant sync failed' });
  }
});

/**
 * DELETE /api/plants/user/clear
 * Deletes all plants and photos for a specific user ID or email in Railway PostgreSQL.
 */
router.delete('/user/clear', async (req: Request, res: Response) => {
  try {
    const userId = (req.query.userId as string) || (req.body?.userId as string) || '';
    const email = (req.query.email as string) || (req.body?.email as string) || '';

    const userIdsToDelete: string[] = [];
    if (userId) userIdsToDelete.push(userId);

    if (email) {
      const users = await prisma.user.findMany({
        where: { email: { contains: email, mode: 'insensitive' } }
      });
      users.forEach(u => userIdsToDelete.push(u.id));
    }

    userIdsToDelete.push('usr_wcsurface_gmail_com', 'usr_google_wcsurface_gmail_com');

    const result = await prisma.plant.deleteMany({
      where: {
        userId: { in: userIdsToDelete }
      }
    });

    return res.status(200).json({
      success: true,
      message: `Cleared ${result.count} plants for user [${email || userId}] from Railway PostgreSQL.`,
      count: result.count
    });
  } catch (error) {
    console.error('Error clearing user plants:', error);
    return res.status(500).json({ success: false, error: 'Failed to clear user plants' });
  }
});

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
    if (user && user.tier === 'FREE') {
      const plantCount = await prisma.plant.count({ where: { userId: currentUserId } });
      if (plantCount >= 2) {
        return res.status(403).json({
          success: false,
          error: 'Free tier maximum plant limit reached (2 plants max). Please upgrade to Pro for unlimited plants.'
        });
      }
    }

    // Call OpenAI Vision Service
    const visionResult = await identifyPlantFromImageKey(currentUserId, imageKey);

    const publicPhotoUrl = `https://${process.env.R2_BUCKET_NAME || 'still-leaf-images'}.r2.cloudflarestorage.com/${imageKey}`;

    // Create plant in PostgreSQL via Prisma
    const newPlant = await prisma.plant.create({
      data: {
        userId: currentUserId,
        nickname: nickname || visionResult.commonName,
        botanicalName: visionResult.botanicalName,
        commonName: visionResult.commonName,
        sunlightRequirement: visionResult.sunlightRequirement,
        idealTemperature: visionResult.idealTemperature,
        idealHumidity: visionResult.idealHumidity,
        waterIntervalDays: visionResult.waterIntervalDays,
        feedIntervalDays: visionResult.feedIntervalDays,
        rotateIntervalDays: visionResult.rotateIntervalDays,
        trimIntervalDays: visionResult.trimIntervalDays,
        careSummary: visionResult.zenSummary,
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
 * Fetches all plants for a user by userId or email.
 */
router.get('/', async (req: Request, res: Response) => {
  try {
    const userId = (req.query.userId as string) || '';
    const email = (req.query.email as string) || '';

    const userIds: string[] = [];
    if (userId) userIds.push(userId);

    if (email && email.trim()) {
      const normalizedEmail = email.trim().toLowerCase();
      try {
        const users = await prisma.user.findMany({
          where: { email: { contains: normalizedEmail } }
        });
        users.forEach(u => userIds.push(u.id));
      } catch (_: any) {}
      userIds.push(`usr_${normalizedEmail.replace(/[^a-z0-9]/g, '_')}`);
      userIds.push(`usr_google_${normalizedEmail.replace(/[^a-z0-9]/g, '_')}`);
    }

    if (userIds.length === 0) {
      return res.status(200).json({ success: true, data: [] });
    }

    const plants = await prisma.plant.findMany({
      where: { userId: { in: userIds } },
      include: {
        photos: true,
        careLogs: true
      }
    });

    return res.status(200).json({ success: true, data: plants });
  } catch (error: any) {
    console.error('Error fetching plants:', error);
    return res.status(200).json({ success: true, data: [] });
  }
});

export default router;
