/*
  Warnings:

  - Added the required column `patient_id` to the `medical_certificate` table without a default value. This is not possible if the table is not empty.

*/
-- AlterTable
ALTER TABLE `medical_certificate` ADD COLUMN `patient_id` INTEGER NOT NULL;

-- AddForeignKey
ALTER TABLE `medical_certificate` ADD CONSTRAINT `medical_certificate_patient_id_fkey` FOREIGN KEY (`patient_id`) REFERENCES `patient`(`patient_id`) ON DELETE RESTRICT ON UPDATE CASCADE;
