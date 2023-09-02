import util from 'util'
import multer from 'multer';


const maxSize = 2 * 1024 * 1024;
let storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, "./uploads/medical_certificates/");
  },
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  },
});

let uploadFile = multer({
  storage: storage,
  limits: { fileSize: maxSize },
}).single("file");

// create the exported middleware object
let uploadFileMiddleware = util.promisify(uploadFile);
export default uploadFileMiddleware;