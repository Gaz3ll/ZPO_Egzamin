const express = require('express');
const path = require('path');
const { sequelize, Table } = require('./models');
const restaurantRoutes = require('./routes/restaurant');

const app = express();
const PORT = process.env.PORT || 3002;

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
      title: 'Restaurant Management API',
      version: '1.0.0',
      description: 'Dokumentacja API systemu zarządzania stolikami w restauracji',
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

app.use(restaurantRoutes);

// Funkcyjne seedowanie tabeli bez pętli i ifów
async function seed() {
  const count = await Table.count();
  count === 0 && await Table.bulkCreate([
    { seats: 2, location: 'INDOOR' },
    { seats: 4, location: 'INDOOR' },
    { seats: 4, location: 'OUTDOOR' },
    { seats: 6, location: 'OUTDOOR' },
    { seats: 8, location: 'INDOOR' },
  ]);
}

sequelize.sync().then(seed).then(() => {
  app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
});

module.exports = app;
