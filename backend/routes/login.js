import express from "express";

import md5 from 'md5';
import { PrismaClient } from "@prisma/client";
import Joi from "joi";
/** Required Node.js modules */
/** Instantiate Prisma client */
const prisma = new PrismaClient();



const router = express.Router()

router.post('/alive',async (req,res)=>{
    let token = req.body.token
    try {
        const session = await prisma.sessions.findUnique({
            where:{
                token:token
            }
        })
        const user = await prisma.users.findUnique({
            where : {
                email: session.email
            }
        })
        if(user.role != 3){
            res.send({"status":"SUCCESS","user_data":user,"token":token})
            return
        }
        const patient_data = await prisma.patient.findUnique({
            where: {
                patient_id: user.user_id
            }
        })
        res.send({"status":"SUCCESS","user_data":user,"patient_data":patient_data,"token":token})
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to authenticate token, maybe the token expired"});
    }
})

router.post('/',async function(req,res){
    let email = req.body.email
    let password = req.body.password
    console.log(email)
    try {
        const user = await prisma.users.findUnique({
            where : {
                email: email
            }
        })
        let passwordHash = user.passwordHash
        if(!verifyPasswordHash(password,passwordHash)){
            res.send({"status":"FAILED","message":"Invalid Credentials!"})
            return
        }
        let token = generateToken();
        const today = new Date();
        const yyyy = today.getFullYear();
        let mm = today.getMonth() + 1; 
        let dd = today.getDate();

        if (dd < 10) dd = '0' + dd;
        if (mm < 10) mm = '0' + mm;

        const formattedToday = yyyy + '-' + mm + '-' + dd;
        const d = new Date(formattedToday)
        await prisma.sessions.create({
            data:{
                email:email,
                token:token,
                generated_at:d
            }
        })

        if(user.role != 3){
            res.send({"status":"SUCCESS","user_data":user,"token":token})
            return
        }
        const patient_data = await prisma.patient.findUnique({
            where: {
                patient_id: user.user_id
            }
        })
        res.send({"status":"SUCCESS","user_data":user,"patient_data":patient_data,"token":token})
    }
    catch(err) {
        console.log(err)
        res.send({"status":"FAILED","message":"Invalid Credentials!"})
    }
    

})

function verifyPasswordHash(rawPassword,passwordHash){
    return md5(rawPassword)==passwordHash
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