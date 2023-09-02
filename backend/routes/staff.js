import express from "express";

import md5 from 'md5';
import { PrismaClient } from "@prisma/client";
import Joi from "joi";
import joiM from 'express-joi-validation'

/** Required Node.js modules */
/** Instantiate Prisma client */
const joiMiddleware = joiM.createValidator()

const prisma = new PrismaClient();

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


const router = express.Router()

router.post('/create-appointment',async (req,res)=>{
    let email = req.body.email
    try{
        const user = await prisma.users.findUnique({
            where: {
                email: email
            }
        })
        let user_id = user.user_id
        res.body = await prisma.appointment.create({
            data:{
                patient_id: user_id,
                reason: req.body.reason,
                schedule_time: req.body.reporting_time,
                reporting_time: req.body.reporting_time,
                status: "APPROVED"
            }
        });
        res.status(200).send({"status":"SUCCESS","message":"OK"})
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to create appointment"})
    }
})



export default router