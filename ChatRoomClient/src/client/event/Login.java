package client.event;

import client.ChatRoomThread;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;

public class Login extends MouseAdapter {
    private JFrame frame;
    private JTextField address;

    public Login(JFrame frame, JTextField address) {
        this.frame = frame;
        this.address = address;
    }
    @Override
    public void mouseClicked(MouseEvent e) {
        String[] addressInfo = address.getText().split(":");
        if(addressInfo.length != 2){
            JOptionPane.showMessageDialog(frame,"请输入正确地服务器地址和端口，例如：127.0.0.1:2345");
        }else{
            try {
                Socket socket = new Socket(addressInfo[0],Integer.valueOf(addressInfo[1]));
                JDialog dialog = new JDialog(frame);
                dialog.setModal(true);
                dialog.setTitle("登录聊天室");
                dialog.setBounds(300,300,300,200);
                dialog.setLayout(null);
                JLabel label1 = new JLabel("账号：");
                label1.setBounds(20,20,60,30);
                JLabel label2 = new JLabel("密码：");
                label2.setBounds(20,60,60,30);
                JTextField user = new JTextField("");
                user.setBounds(90,20,180,30);
                JPasswordField pass = new JPasswordField("");
                pass.setBounds(90,60,180,30);
                JButton button = new JButton("登录");
                button.setBounds(100,110,100,30);
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        String username = user.getText();
                        String password = String.valueOf(pass.getPassword());
                        if(username.length() == 0 || password.length() == 0){
                            JOptionPane.showMessageDialog(dialog,"账户和密码不能为空！");
                        }else{
                            try {
                                BufferedWriter clientOS = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                BufferedReader clientIS = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                clientOS.write(String.format("%s %s\n",username,password));
                                clientOS.flush();
                                // 验证登录结果
                                String receivedMsg = clientIS.readLine();
                                if(receivedMsg.indexOf("successfully") != -1){
                                    JOptionPane.showMessageDialog(dialog,"登录成功！");
                                    // 加入到聊天线程中
                                    new Thread(new ChatRoomThread(socket,clientIS,clientOS,username)).start();
                                    dialog.dispose();
                                    frame.dispose();
                                }else if(receivedMsg.indexOf("error") != -1){
                                    JOptionPane.showMessageDialog(dialog,"登录失败，密码错误！");
                                    clientIS.close();
                                    clientOS.close();
                                    socket.close();
                                    dialog.dispose();
                                }else if(receivedMsg.indexOf("login") != -1){
                                    JOptionPane.showMessageDialog(dialog,"登录失败，此账户已在线！");
                                    clientIS.close();
                                    clientOS.close();
                                    socket.close();
                                    dialog.dispose();
                                }else{
                                    JOptionPane.showMessageDialog(dialog,"登录失败，账户不存在！");
                                    clientIS.close();
                                    clientOS.close();
                                    socket.close();
                                    dialog.dispose();
                                }
                            } catch (IOException ex) {
                                System.out.println("socket发生错误！");
                            }
                        }
                    }
                });
                dialog.add(label1);
                dialog.add(user);
                dialog.add(label2);
                dialog.add(pass);
                dialog.add(button);
                dialog.setVisible(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,"请检查服务器是否启动，服务器地址和端口是否错误！");
            }
        }
    }
}
