# How to Deploy Your Pool Tournament App to Render

I have successfully pushed the complete, tested project to your GitHub repository: `btwomack/pool-tournament-app`.

Because the Render API requires an authentication token and a Workspace ID that are private to your account, I cannot trigger the deployment programmatically. However, the repository now includes a fully configured `render.yaml` Blueprint file, which makes deployment extremely simple.

Please follow these exact steps to deploy both your backend and frontend to Render:

## Step 1: Connect to Render

1. Log in to your account at [Render.com](https://render.com).
2. If you haven't already, ensure your GitHub account is connected to your Render account.

## Step 2: Deploy using the Blueprint

The `render.yaml` file in your repository is a "Blueprint" that tells Render exactly how to set up the database, backend, and frontend, and how to connect them together.

1. On the Render Dashboard, click the **"New +"** button in the top right corner.
2. Select **"Blueprint"** from the dropdown menu.
3. You will see a list of your connected GitHub repositories. Find and select **`btwomack/pool-tournament-app`**.
   * *If you don't see it, click "Configure account" on the right side to grant Render access to this specific repository.*
4. On the next screen, Render will read the `render.yaml` file and show you a summary of the resources it will create:
   * A PostgreSQL database (`pool-tournament-db`)
   * A Web Service for the Spring Boot backend (`pool-backend`)
   * A Static Site for the React frontend (`pool-frontend`)
5. Click **"Apply Blueprint"** at the bottom of the page.

## Step 3: Monitor the Deployment

Render will now begin provisioning the database and building both services.

1. **Database:** This will spin up almost instantly.
2. **Backend (`pool-backend`):** This uses Docker to compile the Java code and run the Spring Boot app. It may take 3-5 minutes to build and start.
3. **Frontend (`pool-frontend`):** This uses Vite to build the React app. It will automatically link its API calls to the backend service.

You can click on the individual services in your dashboard to view their build logs.

## Step 4: Configure Environment Variables (Optional but Recommended)

The Blueprint automatically sets up most of the required environment variables (like database connections and a generated JWT secret). However, to enable full functionality (Payments and SMS), you will need to update the placeholder values in the backend service.

1. Go to your Render Dashboard and click on the **`pool-backend`** web service.
2. Click on **"Environment"** in the left sidebar.
3. Update the following variables with your actual keys:
   * `STRIPE_SECRET_KEY`
   * `STRIPE_WEBHOOK_SECRET`
   * `TWILIO_ACCOUNT_SID`
   * `TWILIO_AUTH_TOKEN`
   * `TWILIO_PHONE_NUMBER`
4. Click **"Save Changes"**. Render will automatically restart the backend with the new keys.

## Step 5: Access Your App

Once both the backend and frontend have finished deploying (showing a green "Live" status):

1. Go to your Render Dashboard and click on the **`pool-frontend`** static site.
2. Click the URL displayed near the top left (e.g., `https://pool-frontend-xxxx.onrender.com`).
3. This is your live Pool Tournament Management App!
