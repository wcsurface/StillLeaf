import express, { Express, Request, Response } from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import plantRoutes from './routes/plantRoutes';
import billingRoutes from './routes/billingRoutes';

dotenv.config();

const app: Express = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Healthcheck endpoint
app.get('/health', (req: Request, res: Response) => {
  return res.status(200).json({
    status: 'ok',
    app: 'Still Leaf Backend Service',
    timestamp: new Date().toISOString()
  });
});

// Route registration
app.use('/api/plants', plantRoutes);
app.use('/api/billing', billingRoutes);

app.listen(PORT, () => {
  console.log(`Still Leaf Backend Server running on port ${PORT}`);
});
