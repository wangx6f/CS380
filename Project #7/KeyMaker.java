import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;

/**
 * Created by wxy03 on 5/31/2017.
 */
public class KeyMaker {

    public static void makekeys()
    {
        try{
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(4096);
            KeyPair keyPair = gen.genKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("public.bin"))))
            {
                oos.writeObject(publicKey);
            }
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("private.bin"))))
            {
                oos.writeObject(privateKey);
            }

        }
        catch (NoSuchAlgorithmException | IOException e)
        {
            e.printStackTrace(System.err);
        }
    }
}
