/*
  Warnings:

  - You are about to drop the column `patient_id` on the `medical_certificate` table. All the data in the column will be lost.

*/
-- DropForeignKey
ALTER TABLE `medical_certificate` DROP FOREIGN KEY `medical_certificate_patient_id_fkey`;

-- AlterTable
ALTER TABLE `medical_certificate` DROP COLUMN `patient_id`;
