/*
  Warnings:

  - You are about to alter the column `status` on the `appointment` table. The data in that column could be lost. The data in that column will be cast from `VarChar(50)` to `Enum(EnumId(1))`.
  - You are about to alter the column `status` on the `medical_certificate` table. The data in that column could be lost. The data in that column will be cast from `Int` to `Enum(EnumId(1))`.

*/
-- AlterTable
ALTER TABLE `appointment` MODIFY `status` ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    MODIFY `reporting_time` VARCHAR(50) NULL,
    MODIFY `treatment_name` VARCHAR(100) NULL,
    MODIFY `prescription_details` TEXT NULL,
    MODIFY `advice` TEXT NULL;

-- AlterTable
ALTER TABLE `medical_certificate` MODIFY `status` ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING';
