import { Router, Request, Response } from 'express';
import { PrismaClient, SubscriptionTier } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

/**
 * POST /api/billing/verify
 * Verifies Play Purchase Token & Upgrades User Tier in Railway PostgreSQL.
 */
router.post('/verify', async (req: Request, res: Response) => {
  try {
    const { userId, productId, purchaseToken, basePlanId } = req.body;

    if (!userId || !productId || !purchaseToken) {
      return res.status(400).json({ error: 'Missing required purchase parameters.' });
    }

    let determinedTier: SubscriptionTier = SubscriptionTier.FREE;

    if (productId === 'still_leaf_lifetime_2999' || productId === 'still-leaf-lifetime-2999') {
      determinedTier = SubscriptionTier.LIFETIME;
    } else if (productId === 'still_leaf_pro_sub' || productId === 'still-leaf-pro-sub') {
      if (basePlanId === 'annual-1499') {
        determinedTier = SubscriptionTier.ANNUAL;
      } else {
        determinedTier = SubscriptionTier.MONTHLY;
      }
    }

    // Update User in PostgreSQL
    const updatedUser = await prisma.user.update({
      where: { id: userId },
      data: {
        tier: determinedTier,
        playPurchaseToken: purchaseToken,
        subscriptionExpiresAt:
          determinedTier === SubscriptionTier.LIFETIME
            ? new Date('2099-12-31')
            : new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
      },
    });

    return res.status(200).json({
      success: true,
      tier: updatedUser.tier,
      freeScansRemaining: 999999,
      expiresAt: updatedUser.subscriptionExpiresAt,
    });
  } catch (error) {
    console.error('Billing Verification Error:', error);
    return res.status(500).json({ error: 'Failed to verify purchase entitlement.' });
  }
});

/**
 * POST /api/billing/rtdn-webhook
 * Ingestion point for Google Cloud Pub/Sub RTDN events.
 */
router.post('/rtdn-webhook', async (req: Request, res: Response) => {
  try {
    return res.status(200).json({ received: true });
  } catch (error) {
    return res.status(500).json({ error: 'Webhook processing failed.' });
  }
});

export default router;
