import express, { Express, Request, Response } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import plantRoutes from './routes/plantRoutes';
import billingRoutes from './routes/billingRoutes';
import authRoutes from './routes/authRoutes';
import { identifyPlantFromDualBase64 } from './services/plantVisionService';

dotenv.config();

const app: Express = express();
const PORT = process.env.PORT || 3000;

app.use(cors());

// Increase payload limits to 50MB for dual base64 camera photo payloads
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// Healthcheck endpoint
app.get('/health', (req: Request, res: Response) => {
  return res.status(200).json({
    status: 'ok',
    app: 'Still Leaf Backend Service',
    timestamp: new Date().toISOString()
  });
});

// Dual-photo botanical vision handler
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
    console.error('Error identifying plant via base64:', error);
    return res.status(500).json({ success: false, error: error?.message || 'Plant identification failed' });
  }
};

// Register vision endpoints for both identify-dual and identify-base64
app.post('/api/plants/identify-dual', handleDualVision);
app.post('/api/plants/identify-base64', handleDualVision);

// Route registration
app.use('/api/auth', authRoutes);
app.use('/api/plants', plantRoutes);
app.use('/api/billing', billingRoutes);

app.listen(PORT, () => {
  console.log(`Still Leaf Backend Server running on port ${PORT}`);
});
