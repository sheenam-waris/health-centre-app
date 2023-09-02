import express from "express";
import uploadFile from './upload_prescription.js';
import { PrismaClient } from "@prisma/client";
import Joi from "joi";
import joiM from 'express-joi-validation'
/** Required Node.js modules */
/** Instantiate Prisma client */
const joiMiddleware = joiM.createValidator()
const prisma = new PrismaClient();

const router = express.Router()

const appointmentSchema = Joi.object({
    patient_id: Joi.number(),
    reason: Joi.string(),
    schedule_time: Joi.string(),
    status: Joi.string(),
    reporting_time: Joi.string(),
    treatment_name: Joi.string(),
    prescription_details: Joi.string(),
    advice: Joi.string()
})

router.get("/", async function(req,res){
    try {
        const appointments = await prisma.appointment.findMany()
       
        res.status(200).send({"status":"SUCCESS","appointments":appointments})
    }
    catch(err) {
        res.status(500).send({"status":"FAILED","message":"FAILED"})
    }
})

router.post("/get-by-id",async (req,res)=>{
    let app_id = req.body.app_id;
    try{
        const app = await prisma.appointment.findUnique({
            where:{
                app_id:app_id
            }
        })
        res.status(200).send({"status":"SUCCESS","appointment":app})
    }catch(err){
        res.send({"status":"FAILED","message":"FAILED"})
    }
})

router.post("/",joiMiddleware.query(appointmentSchema),async function(req,res){
    let user_id = req.body.user_id
    try{
        const user = await prisma.users.findUnique({
            where:{
                user_id:user_id
            }
        })
        if(user.role != 3) {
            res.send({"status":"FAILED","message":"UNAUTHORIZED OPERATION!"})
            return
        }
    }catch(err){
        res.send({"status":"FAILED","message":"FAILED OR UNAUTHORIZED"})
    }
    try{
        res.body = await prisma.appointment.create({
            data:{
                patient_id: user_id,
                reason: req.body.reason,
                schedule_time: req.body.schedule_time,
            }
        });
        res.status(200).send({"status":"SUCCESS","message":"OK"})
    }catch(err){
        console.log(err)
        res.status(500).send({"status":"FAILED","message":"FAILED OR UNAUTHORIZED"})
    }
})

router.post('/approve',joiMiddleware.query(appointmentSchema),async function(req,res){
    let user_id = req.body.user_id
    try{
        const user = await prisma.users.findUnique({
            where:{
                user_id:user_id
            }
        })
        if(user.role == 4) {
            res.send({"status":"FAILED","message":"UNAUTHORIZED OPERATION!"})
            return
        }
    }catch(err){
        res.send({"status":"FAILED","message":"FAILED OR UNAUTHORIZED"})
    }
    try{
        const app = await prisma.appointment.update({
            where : {
                app_id: req.body.app_id
            },
            data: {
                status: req.body.status,
                reporting_time: req.body.reporting_time
            }
        })
        res.status(200).send({"status":"SUCCESS","body":app})
    }catch(err){
        res.send({"status":"FAILED","message":"FAILED OR UNAUTHORIZED"})
    }

})

router.get('/approved',async (req,res)=>{
    try{
        const approvedAppointments = await prisma.appointment.findMany({
            where: {
                status: "APPROVED"
            }
        })
        res.status(200).send({"status":"SUCCESS","approved_appointments":approvedAppointments})
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to fetch the appointments!"})
    }
})

router.post('/upload-prescription',async (req,res)=>{
    try {
        const app_id = req.query.app_id
        const treatment_name = req.query.treatment_name
        await uploadFile(req, res);
        if (req.file == undefined) {
          return res.status(400).send({ message: "Upload a file please!" });
        }
        console.log(app_id)
        const appointment = await prisma.appointment.update({
            where:{
                app_id:Number(app_id)
            },
            data:{
                visited:true,
                treatment_name: treatment_name
            }
        })

        res.status(200).send({
          message: "The following file was uploaded successfully: " + req.file.originalname,
        });
      } catch (err) { 
        console.log(err)
        if (err.code == "LIMIT_FILE_SIZE") {
            return res.status(500).send({
              message: "File larger than 2MB cannot be uploaded!",
            });
          }
        res.status(500).send({
          message: `Unable to upload the file: ${req.file.originalname}. ${err}`,
        });
      }
})

router.get('/download',async (req,res)=>{
    const filename = req.query.filename
    const file_dir = './uploads/prescriptions/'
    
    res.download(file_dir+filename,filename,(err)=>{
        if(err){
            res.status(500).send({
                message: "Could not download the file. " + err,
                });
        }
    });
    
})

router.post('/remove-by-id',async (req,res)=>{
    let id = req.body.app_id
    try {
        await prisma.appointment.delete({
            where:{
                app_id:id
            }
        })
        await prisma.medical_certificate.delete({
            where:{
                app_id:id
            }
        })
        res.send({"status":"SUCCESS","message":"Deleted"})
    }catch(err){
        console.log(err)
        res.send({"status":"FAILED","message":"Failed to remove appointment"})
    }
})

export default router