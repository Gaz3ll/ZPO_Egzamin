const express = require('express');
const path = require('path');
const { sequelize, Meal } = require('./models');
const dietRoutes = require('./routes/diet');

const app = express();
const PORT = process.env.PORT || 3003;

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

// Konfiguracja Swagger UI dla API REST
const swaggerUi = require('swagger-ui-express');
const swaggerJsdoc = require('swagger-jsdoc');

const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Diet Tracker API',
      version: '1.0.0',
      description: 'Dokumentacja API systemu kalkulatora zapotrzebowania kalorycznego',
    },
    components: {
      securitySchemes: {
        basicAuth: {
          type: 'http',
          scheme: 'basic',
        },
      },
    },
    security: [
      {
        basicAuth: [],
      },
    ],
  },
  apis: [path.join(__dirname, 'routes', '*.js')],
};

const swaggerSpec = swaggerJsdoc(swaggerOptions);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.use(dietRoutes);

// Funkcyjne seedowanie posiłków
async function seed() {
  const count = await Meal.count();
  count === 0 && await Meal.bulkCreate([
    { name: 'Jajecznica', proteins: 15, carbs: 1, fats: 12, calories: 172 },
    { name: 'Pierś z kurczaka z ryżem', proteins: 40, carbs: 60, fats: 8, calories: 472 },
    { name: 'Odżywka białkowa', proteins: 24, carbs: 3, fats: 2, calories: 126 },
    { name: 'Sałatka Cezar', proteins: 12, carbs: 10, fats: 18, calories: 250 },
  ]);
}

sequelize.sync().then(seed).then(() => {
  app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
});

module.exports = app;
