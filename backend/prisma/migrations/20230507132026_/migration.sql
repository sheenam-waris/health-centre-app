/*
  Warnings:

  - A unique constraint covering the columns `[app_id]` on the table `medical_certificate` will be added. If there are existing duplicate values, this will fail.

*/
-- CreateIndex
CREATE UNIQUE INDEX `medical_certificate_app_id_key` ON `medical_certificate`(`app_id`);
