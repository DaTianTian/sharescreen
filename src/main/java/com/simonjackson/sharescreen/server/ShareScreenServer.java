package com.simonjackson.sharescreen.server;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * 屏幕共享服务端
 * @author SimonJackson
 * */
public class ShareScreenServer {
    private static int size = 4;//分割粒度,比如size=4,会把屏幕实时分割为4*4共16个图片进行传输，建议不低于4,因为UDP传输数据包有大小限制，最大不超过65536字节
    private static String host = "10.10.19.166";
    private static int port = 12345;
    public static void main(String[] args) throws IOException, AWTException, InterruptedException{
        DatagramSocket ds = new DatagramSocket(port);
        Robot robot = new Robot();
        byte[] start = "start".getBytes();
        byte[] end = "end".getBytes();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        byte[] head = (dimension.width+","+dimension.height).getBytes();
        DatagramPacket dp = new DatagramPacket(head, head.length, InetAddress.getByName(host),port);
        ds.send(dp);
        Thread.sleep(2000);
        while(true){
            java.util.List<Tuple> list = batchSnapshot(robot);
            for(Tuple tuple:list){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        byte[] bytes = tuple.out.toByteArray();
                        try{
                            DatagramPacket dp = new DatagramPacket(bytes, bytes.length,InetAddress.getByName(host),12345);
                            ds.send(dp);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    private static ByteArrayOutputStream snapshot(Robot robot) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage bufferedImage= robot.createScreenCapture(new Rectangle(new Dimension(dimension.width/4, dimension.height/4)));
        ImageIO.write(bufferedImage, "jpg", out);
        return out;
    }

    private static java.util.List<Tuple> batchSnapshot(Robot robot) throws IOException{
        List<Tuple> list = new ArrayList<>();
        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                Rectangle rectangle = new Rectangle(i*dimension.width/size,j*dimension.height/size,dimension.width/size, dimension.height/size);
                BufferedImage bufferedImage= robot.createScreenCapture(rectangle);
                out.write(new byte[]{(byte)(i*size+j)});
                ImageIO.write(bufferedImage, "jpg", out);
                list.add(new Tuple(out,i*size+j));
            }
        }
        return list;
    }
    static class Tuple{
        ByteArrayOutputStream out;
        int index;
        Tuple(ByteArrayOutputStream out,int index){
            this.out = out;
            this.index = index;
        }
    }
}
