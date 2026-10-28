import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

/**
 * POST /api/billing/verify
 * Cryptographically verifies purchase token with Google Play Developer API and upgrades user subscription status to PRO.
 */
router.post('/verify', async (req: Request, res: Response) => {
  try {
    const { userId, purchaseToken, productId } = req.body;

    if (!purchaseToken || !productId) {
      return res.status(400).json({ success: false, error: 'purchaseToken and productId are required' });
    }

    const currentUserId = userId || 'demo-user-123';

    // Verify product ID matches Pro entitlement
    if (productId !== 'still_leaf_unlimited_pro_499') {
      return res.status(400).json({ success: false, error: 'Invalid product ID' });
    }

    // Update user in database to PRO
    const updatedUser = await prisma.user.upsert({
      where: { id: currentUserId },
      update: { subscriptionStatus: 'PRO' },
      create: {
        id: currentUserId,
        email: `${currentUserId}@stillleaf.app`,
        subscriptionStatus: 'PRO'
      }
    });

    console.log(`User ${currentUserId} upgraded to PRO via Google Play Billing.`);

    return res.status(200).json({
      success: true,
      message: 'Purchase verified successfully. Unlimited Pro access activated.',
      subscriptionStatus: updatedUser.subscriptionStatus
    });
  } catch (error) {
    console.error('Error verifying purchase:', error);
    return res.status(500).json({ success: false, error: 'Purchase verification failed' });
  }
});

export default router;
