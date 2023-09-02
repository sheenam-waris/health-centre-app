import express from "express";

import md5 from 'md5';
import { PrismaClient } from "@prisma/client";

const prisma = new PrismaClient();

const router = express.Router()


router.post('/',async (req,res)=>{
    let token =  req.body.token;
    try {
        await prisma.sessions.delete({
            where:{
                token:token
            }
        })
        res.send({"status":"SUCCESS","message":"Logged out!"});
    }catch(err){
        res.send({"status":"FAILED","message":"Failed to logout"});
    }
})

export default router;