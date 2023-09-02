import express from "express";

import { PrismaClient } from "@prisma/client";
import Joi from "joi";
import joiM from 'express-joi-validation'
import md5 from "md5";
/** Required Node.js modules */
/** Instantiate Prisma client */
const joiMiddleware = joiM.createValidator()
const prisma = new PrismaClient();

const router = express.Router()

const userSchema = Joi.object({
    name: Joi.string(),
    email:Joi.string(),
    passwordHash:Joi.string(),
    dob:Joi.string(),
    gender:Joi.string(),
    phone:Joi.string(),
    role:Joi.number()
})

const patientSchema = Joi.object({
    patient_id:Joi.number(),
    rollno:Joi.string(),
    address:Joi.string(),
    hostel_details:Joi.string()
})

router.post('/account-exists',async (req,res)=>{
    let email = req.body.email
    try {
        const user = await prisma.users.findUnique({
            where:{
                email:email
            }
        })
        let token = "none"
        try{
            const session = await prisma.sessions.findFirst({
                where:{
                    email:email
                }
            })
            token = session.token
        }catch(err){
            //no session, create one
            token = generateToken()
            let date = getTodayDate()
            await prisma.sessions.create({
                data:{
                    email:email,
                    token:token,
                    generated_at:date
                }
            })
        }

        if(user!=null){
            let role = user.role
            if(role!=3)
                res.send({"status":"SUCCESS","user":user,"token":token})
            else{
                const patient = await prisma.patient.findFirst({
                    where:{
                        patient_id:user.user_id
                    }
                })
                
               res.send({"status":"SUCCESS","user":user,"patient":patient,"token":token})
            }
        }    
        else{
            
            res.send({"status":"FAILED","message":"Email already exists!"})
        }
       
    }catch(err){
        console.log(err)
        res.send({"status":"FAILED","message":"Something went wrong!"})
    }
})

router.post('/create-account',joiMiddleware.query(userSchema),joiMiddleware.query(patientSchema),async (req,res)=>{
    let name = req.body.name
    let email = req.body.email
    let password = req.body.password
    let dob = req.body.dob
    let gender = req.body.gender
    let phone = req.body.phone
    let role = req.body.role


    try {
        let u = await prisma.users.findUnique({
            where: {
                email:email
            }
        })
        if(u != undefined){
            res.send({"status":"FAILED","message":"This email is already associated with an account!"})
            return
        }else{

        }
    } catch (err) {
        console.log(err)
        res.send({"status":"FAILED"})
    }

    let passwordHash = md5(password)
    var user
    try {
        user = await prisma.users.create({
           data:{
                name:name,
                email:email,
                passwordHash:passwordHash,
                dob:dob,
                gender:gender,
                phone:phone,
                role:role
           } 
        })
        console.log(user)
    } catch (err) {
        res.send({"status":"FAILED","message":"Failed to create a user account!"})
        return
    }

    if(role != 3){
        //If user is not a patient
        res.send({"status":"SUCCESS","message":"Successfully created a user account!"})
        return
    }else if(role == 3){
        //If user is a patient
        let rollno = req.body.rollno
        let address = req.body.address
        let hostel_details = req.body.hostel_details

        try {
            await prisma.patient.create({
                data:{
                    patient_id:user.user_id,
                    rollno:rollno,
                    address:address,
                    hostel_details:hostel_details
                }
            })
            res.send({"status":"SUCCESS","message":"Successfully createa a user and associated patient account"})
        } catch (err) {
            console.log(err)
            res.send({"status":"FAILED","message":"Failed to create a patient account!"})
        }
    }else{
        res.send({"status":"FAILED","message":"Invalid role"})
        return
    }
})

router.post('/remove-by-id',async (req,res)=>{
    let id = req.body.user_id
    try {
        await prisma.users.delete({
            where:{
                user_id:id
            }
        })
        
        res.send({"status":"SUCCESS","message":"Deleted"})
    }catch(err){
        console.log(err)
        res.send({"status":"FAILED","message":"Failed to delete user"})
    }
})


router.post('/appointments',async (req,res)=>{
    let user_id = req.body.user_id;
    try{
        const myappointments = await prisma.appointment.findMany({
            where: {
                patient_id : user_id
            }
        });
        res.send({"status":"SUCCESS","appointments":myappointments});
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to fetch appointments"})
    }
})

router.post('/get-user',async (req,res)=>{
    let user_id = req.body.user_id;
    try{
        const user = await prisma.users.findUnique({
            where: {
                user_id : user_id
            }
        });
        
        if(user.role != 3){
            res.send({"status":"SUCCESS","user":user})
            return
        }
        const patient_data = await prisma.patient.findUnique({
            where: {
                patient_id: user.user_id
            }
        })
        res.send({"status":"SUCCESS","user":user,"patient_data":patient_data})
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to fetch user"})
    }
})

router.post('/update-profile',async (req,res)=>{
    let user_id = req.body.user_id;
    try{
        const user = await prisma.users.update({
            where:{
                user_id:user_id
            },
            data:{
                gender:req.body.gender,
                dob:req.body.dob,
                phone:req.body.phone
            }
        })
        if(user.role == 3){
            try {
                    await prisma.patient.update({
                    where:{
                        patient_id:user_id
                    },
                    data:{
                        hostel_details:req.body.hostel_details,
                        address:req.body.address,
                        rollno:req.body.rollno
                    }
                })
                res.send({"status":"SUCCESS","message":"Successfully updated user/patient profile"})
            }catch(err){
                res.send({"status":"FAILED","message":"Failed to update patient profile data"})
            }
            
        }else{
            res.send({"status":"SUCCESS","message":"Successfully updated user profile"})
        }
        
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to update profile"})
    }
})

router.post('/admin-update-profile',async (req,res)=>{
    let user_id = req.body.user_id;
    try{
        const user = await prisma.users.update({
            where:{
                user_id:user_id
            },
            data:{
                name:req.body.name,
                email:req.body.email,
                gender:req.body.gender,
                dob:req.body.dob,
                phone:req.body.phone
            }
        })
        res.send({"status":"SUCCESS","message":"Successfully updated user profile"})
        
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to update profile"})
    }
})

router.post('/get-by-role',async (req,res)=>{
    let role = req.body.role
    try{
        const users = await prisma.users.findMany({
            where:{
                role:role
            }
        })
        res.status(200).send({"status":"SUCCESS","users":users})
    }catch(err){
        res.status(500).send({"status":"FAILED","message":"Failed to fetch users by role"})
    }
})

function getTodayDate(){
    const today = new Date();
    const yyyy = today.getFullYear();
    let mm = today.getMonth() + 1; 
    let dd = today.getDate();

    if (dd < 10) dd = '0' + dd;
    if (mm < 10) mm = '0' + mm;

    const formattedToday = yyyy + '-' + mm + '-' + dd;
    const d = new Date(formattedToday)
    return d;
}
function generateToken(){
    return makeid(256)
}
function makeid(length) {
    let result = '';
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    let counter = 0;
    while (counter < length) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
      counter += 1;
    }
    return result;
}


export default router