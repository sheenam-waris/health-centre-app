/*
  Warnings:

  - Added the required column `app_id` to the `medical_certificate` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE `medical_certificate` ADD COLUMN `app_id` INTEGER NOT NULL;
