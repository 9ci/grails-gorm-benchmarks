CREATE TABLE city1M
(
  "id" BIGINT AUTO_INCREMENT PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL,
  "shortCode" VARCHAR(255) NOT NULL,
  "latitude" FLOAT NOT NULL,
  "longitude" FLOAT NOT NULL,
  "country.id" BIGINT NOT NULL,
  "region.id" BIGINT NOT NULL
);