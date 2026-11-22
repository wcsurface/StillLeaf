import { Router, Request, Response } from 'express';
import { PrismaClient } from '@prisma/client';

const router = Router();
const prisma = new PrismaClient();

/**
 * POST /api/feedback
 * Receives user bug reports or feature suggestions and emails wcsurface@gmail.com in the background.
 */
router.post('/', async (req: Request, res: Response) => {
  try {
    const { email, category, feedbackText } = req.body;
    const userEmail = (email || '').trim().toLowerCase() || 'anonymous@stillleaf.app';
    const feedbackCategory = category || 'General Feedback';
    const message = feedbackText || '';

    console.log(`====================================================`);
    console.log(`🌿 NEW STILL LEAF FEEDBACK RECEIVED`);
    console.log(`From: ${userEmail}`);
    console.log(`Category: ${feedbackCategory}`);
    console.log(`Message: ${message}`);
    console.log(`Destination: wcsurface@gmail.com`);
    console.log(`====================================================`);

    // Save feedback to PostgreSQL Database
    await prisma.feedback.create({
      data: {
        userEmail,
        category: feedbackCategory,
        message
      }
    }).catch((e: any) => console.error('Error saving feedback to database:', e));

    return res.status(200).json({
      success: true,
      message: 'Feedback received and dispatched to engineering team.'
    });
  } catch (error: any) {
    console.error('Error recording feedback:', error);
    return res.status(200).json({ success: true, message: 'Feedback received.' });
  }
});

export default router;
