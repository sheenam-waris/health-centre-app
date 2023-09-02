import loginRoute from './routes/login.js'
import appointmentRoute from './routes/appointment.js'
import express from 'express'
import userRoute from './routes/user.js'
import medicalCertRoute from './routes/medical_certificate.js'
import staffRoute from './routes/staff.js'
import logoutRoute from './routes/logout.js'
import { PrismaClient } from "@prisma/client"

const prisma = new PrismaClient();

const app = express();
const PORT = 8080;
const HOST = "0.0.0.0";

app.use(express.json())
app.use(function(req,res,next){
    res.header("Access-Control-Allow-Origin","*");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  next();
});

app.use('/login',loginRoute)
app.use('/appointments',appointmentRoute)
app.use('/user',userRoute)
app.use('/med-cert',medicalCertRoute)
app.use('/staff',staffRoute)
app.use('/logout',logoutRoute)
app.get('/',function(req,res){
    res.send("server-alive");
});


app.listen(PORT,HOST,()=>console.log())

setInterval(invalidateExpiredTokens,1800000);

async function invalidateExpiredTokens(){
  //console.log("checking for expired tokens...")
    const sessions = await prisma.sessions.findMany({});
      let currDate = new Date();
      sessions.forEach(async session => {
          var gen_date = new Date(session.generated_at)
          //console.log(gen_date)
          const diffTime = Math.abs(currDate - gen_date);
          const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
          if(diffDays>=7){
            console.log("clearing token for email "+session.email)
              await prisma.sessions.delete({
                where:{
                  token:session.token
                }
              })
          }
      });

}