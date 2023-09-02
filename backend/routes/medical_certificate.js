import express from "express";
import uploadFile from './upload_medcert.js';
import { PrismaClient } from "@prisma/client";
import Joi from "joi";
import joiM from 'express-joi-validation'

const joiMiddleware = joiM.createValidator()
const prisma = new PrismaClient();

const router = express.Router()

const certificateSchema = Joi.object({
  mc_id: Joi.number(),
  app_id: Joi.number(),
  purpose: Joi.string(),
  duration: Joi.string(),
  requested_at: Joi.string(),
  approved_at: Joi.string(),
  patient_id: Joi.number(),
  status: Joi.string()
})

router.get('/by-id',async (req,res)=>{
    const cert_id = req.body.cert_id
    try{
        const cert = await prisma.medical_certificate.findUnique({
            where:{
                mc_id:cert_id
            }
        })
        res.status(200).send({"status":"SUCCESS","certificate":cert})
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to fetch the certificate"})
    }
})

router.get('/',async (req,res)=>{
    try {
        const medical_certificates = await prisma.medical_certificate.findMany()
       
        res.status(200).send({"status":"SUCCESS","certificates":medical_certificates})
    }
    catch(err) {
        res.status(500).send({"status":"FAILED","message":"FAILED"})
    }
})

router.post('/request-new',joiMiddleware.query(certificateSchema),async(req,res)=>{
    let app_id = req.body.app_id
    let patient_id = req.body.patient_id
    let duration = req.body.duration
    let purpose = req.body.purpose
    const today = new Date();
    const yyyy = today.getFullYear();
    let mm = today.getMonth() + 1; 
    let dd = today.getDate();

    if (dd < 10) dd = '0' + dd;
    if (mm < 10) mm = '0' + mm;

    const formattedToday = dd + '/' + mm + '/' + yyyy;
    try{

        await prisma.medical_certificate.create({
          data:{
              app_id: app_id,
              patient_id: patient_id,
              duration: duration,
              purpose: purpose,
              //status: "PENDING",
              requested_at: formattedToday
          }
        })
        res.send({"status":"SUCCESS","message":"Successfully added a request for med-cert"})
    }catch(err){
      console.log(err)
      res.send({"status":"FAILED","message":"Failed to create a request for med-cert"})
    }
})

router.post('/approve',async (req,res)=>{
  let mc_id = req.body.mc_id;
  let name = req.body.dr_name;

  const today = new Date();
    const yyyy = today.getFullYear();
    let mm = today.getMonth() + 1; 
    let dd = today.getDate();

    if (dd < 10) dd = '0' + dd;
    if (mm < 10) mm = '0' + mm;

    const formattedToday = dd + '/' + mm + '/' + yyyy;
  try {
    await prisma.medical_certificate.update({
      where:{
        mc_id:mc_id
      },
      data:{
        status:"APPROVED",
        approved_by: name,
        approved_at: formattedToday
      }
    })
    res.send({"status":"SUCCESS","message":"Successfully approved med cert"});
  }catch(err){
    res.send({"status":"FAILED","message":"Failed to approve medical certificate"});
  }
})

router.post('/upload',async (req,res)=>{
    try {
        let mc_id = Number(req.query.mc_id)
        await uploadFile(req, res);
        if (req.file == undefined) {
          return res.status(400).send({ message: "Upload a file please!" });
        }
        await prisma.medical_certificate.update({
          where:{
            mc_id:mc_id
          },
          data:{
            status:"APPROVED"
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

router.post('/get-status',async (req,res)=>{
  const app_id = req.body.app_id
  try {
      const cert = await prisma.medical_certificate.findFirst({
        where:{
          app_id:app_id
        }
      });
      res.send({"status":"SUCCESS","certificate_status":cert.status,"purpose":cert.purpose,
      "duration":cert.duration,
        "approved_at":cert.approved_at,
          "approved_by":cert.approved_by})
  }catch(err){
    console.log(err)
      res.send({"status":"FAILED","message":"Failed to get status of med-cert"})
  }
})

router.post('/remove-by-id',async (req,res)=>{
  let cert_id = req.body.cert_id
  try {
      await prisma.medical_certificate.delete({
          where:{
              mc_id:cert_id
          }
      })
      res.send({"status":"SUCCESS","message":"Deleted"})
  }catch(err){
      console.log(err)
      res.send({"status":"FAILED","message":"Failed to remove appointment"})
  }
})

router.get('/download',async (req,res)=>{
    const filename = req.query.filename
    const file_dir = './uploads/medical_certificates/'
    
    res.download(file_dir+filename,filename,(err)=>{
        if(err){
            res.status(500).send({
                message: "Could not download the file. " + err,
                });
        }
    });
    
})



export default router;