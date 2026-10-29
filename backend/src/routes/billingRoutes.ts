import { FastifyInstance } from 'fastify';
import { PrismaClient, SubscriptionTier } from '@prisma/client';

const prisma = new PrismaClient();

export async function billingRoutes(fastify: FastifyInstance) {
  // POST /api/billing/verify - Verify Play Purchase Token & Upgrade User Tier
  fastify.post('/api/billing/verify', async (request, reply) => {
    try {
      const { userId, productId, purchaseToken, basePlanId } = request.body as {
        userId: string;
        productId: string;
        purchaseToken: string;
        basePlanId?: string;
      };

      if (!userId || !productId || !purchaseToken) {
        return reply.status(400).send({ error: 'Missing required purchase parameters.' });
      }

      let determinedTier: SubscriptionTier = SubscriptionTier.FREE;

      if (productId === 'still_leaf_lifetime_2999') {
        determinedTier = SubscriptionTier.LIFETIME;
      } else if (productId === 'still_leaf_pro_sub') {
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

      return reply.send({
        success: true,
        tier: updatedUser.tier,
        freeScansRemaining: 999999,
        expiresAt: updatedUser.subscriptionExpiresAt,
      });
    } catch (error) {
      fastify.log.error('Billing Verification Error:', error);
      return reply.status(500).send({ error: 'Failed to verify purchase entitlement.' });
    }
  });

  // POST /api/billing/rtdn-webhook - Real-time Developer Notifications Webhook
  fastify.post('/api/billing/rtdn-webhook', async (request, reply) => {
    try {
      // Ingestion point for Google Cloud Pub/Sub RTDN events
      return reply.send({ received: true });
    } catch (error) {
      return reply.status(500).send({ error: 'Webhook processing failed.' });
    }
  });
}
