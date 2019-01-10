package gagaBot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class BotBrain {
	
	private Socket mySocket;
	private InputStream myInput;
	private OutputStream myOutput;
	private HashMap<String,ArrayList<String>> quoteMap;
	
	public void InitSocket(String ip, int port) {
        try {
            mySocket = new Socket(ip, port);
            System.out.println(ip + " 연결됨");

            myInput = mySocket.getInputStream();//소켓으로부터 입력받는
            myOutput = mySocket.getOutputStream();//소켓으로 보내는
            
            this.SocketSend("Ag12");
    		this.SocketSend("L(e[∵]-c)|###new_rlacoxld");
    		this.SocketRecv();
    		/*
    		this.ChangeNick("(e[∵]-c)");
    		this.ChangeNick("(e[∵]-c)");
            */
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
	
	public void CloseSocket() {
		try {
			mySocket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void SocketRecv(){
		try {
			int readbyte = 0;
			byte[] ba = new byte[10240];
			readbyte = myInput.read(ba);
			if(readbyte>0) {				
				String bts = new String(ba,0,readbyte-1, "UTF-8");
				System.out.println("recv: " + bts);
				this.DataHandler(bts);
			}
		} catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
        	e.printStackTrace();
        }
	}
	
	public void SocketSend(String msg) {
		try {
			byte[] ba = new byte[65535];
			ByteBuffer sendByteBuffer = ByteBuffer.wrap(ba);
			sendByteBuffer.put(msg.getBytes("utf-8"));
			ba[msg.getBytes("utf-8").length]=0x00;
			myOutput.write(sendByteBuffer.array(),0,msg.getBytes("utf-8").length+1);
			myOutput.flush();
			System.out.println("send :" + msg);
		} catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
	}
	
	public void SendMessage(String msg) {
		this.SocketSend("#" + msg);
	}
	
	public void SendMessage(String msg, String Nickname) {
		this.SocketSend("M" + Nickname + "| " + msg);
	}
	
	public void ChangeNick(String msg) {
		this.SocketSend("N" + msg);
	}
	
	public void DataHandler(String rawdata) {
		String cmd = new String();
		String msg = new String();
		cmd = rawdata.substring(0, 1);
		msg = rawdata.substring(1,rawdata.length());
		if(cmd.equals("C")) {
			this.NickTrace(msg);
		}else if(cmd.equals(":")) {
			if(msg.split("\\|", 3)[1].equals("(e[∵]-c)")) {
			}else if(msg.split("\\|", 3)[2].equals("하이 봇")) {
				this.SendMessage(msg.split("\\|", 3)[1] + " Hawi^^");
			}else if(msg.split("\\|", 3)[2].equals("안녕 도실?")) {
				this.SendMessage("안녕 조");
			}else{
				this.BotDic(msg.split("\\|", 3)[1],msg.split("\\|", 3)[2],0);
			}			
		}else if(cmd.equals("M")) {
			this.BotDic(msg.split("\\|", 3)[0],msg.split("\\|", 3)[1].substring(1),1);
		}
	}
	
	public void initFiles() {
		File fQuotes = new File("quotes.txt");
		File fIOlog = new File("IOLog.txt");
		try { 
			fQuotes.createNewFile();
			fIOlog.createNewFile();
		}catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		Gson jsonreader = new GsonBuilder().create();
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		String str = new String();
		try {
			fis = new FileInputStream("quotes.txt");
			bis = new BufferedInputStream(fis);
			byte[] rb = new byte[102400];
			ByteBuffer bf = ByteBuffer.wrap(rb);
			int bufpos=0;
			while(true) {
				bufpos = bis.read(bf.array(),0,bf.array().length);
				if (bufpos==-1) {
					break;
				}
				str = new String(bf.array(),"UTF-8");
				bf.clear();
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				bis.close();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		JsonReader reader = new JsonReader(new StringReader(str));
		reader.setLenient(true);
		Type type = new TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();
		this.quoteMap = jsonreader.fromJson(reader, type);
	}
	
	public void UpdateQuotes() {
		Gson gson = new Gson();
		String str = gson.toJson(this.quoteMap);
		try {
		Files.write(Paths.get("quotes.txt"),str.getBytes("UTF-8"));
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void NickTrace(String Nickname) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println(dateFormat.format(date));
		String str = dateFormat.format(date) + " " + Nickname+ "\r\n";
		try {
		Files.write(Paths.get("IOLog.txt"),str.getBytes("UTF-8"),StandardOpenOption.APPEND);
		}catch (IOException e) {
			e.printStackTrace();
		}
		this.SendMessage(" " + Nickname + " 하위^^");
	}
	
	public void BotDic(String Nickname, String quote, int flag) {
		try {
			String cmcm = quote.substring(0, 2);
			String mgmg = quote.substring(2,quote.length());
			String mgmg1 = new String();
			String mgmg2 = new String();
			String polQuote = new String();
			ArrayList<String> quotebuf = new ArrayList<>();
			int searchIndex = 0;	
			if(cmcm.equals("~!")) {//input
				searchIndex = mgmg.indexOf(" ");
				if(searchIndex != -1) {
					mgmg1 = mgmg.substring(0, searchIndex);
					mgmg2 = mgmg.substring(searchIndex+1, mgmg.length());
				}else {
					mgmg1 = mgmg;
				}
				polQuote = mgmg2 + " (" + Nickname + ")";
				if(searchIndex != -1 && mgmg.length()!=0 && mgmg2.length()>0) {
					if(this.quoteMap.containsKey(mgmg1)) {
						if(this.quoteMap.get(mgmg1).contains(polQuote)) {
							if(flag==0) {
								this.SendMessage("이미 저장되어 있습니다용");
							}else if(flag==1) {
								this.SendMessage("이미 저장되어 있습니다용",Nickname);
							}
						}else {
							quotebuf = this.quoteMap.get(mgmg1);
							quotebuf.add(polQuote);
							this.quoteMap.put(mgmg1, quotebuf);
							if(flag==0) {
								this.SendMessage(mgmg1 + " 저장됨 - " + polQuote);								
							}else if(flag==1) {
								this.SendMessage(mgmg1 + " 저장됨 - " + polQuote, Nickname);
							}
							
						}
					}else {
						quotebuf.add(polQuote);
						this.quoteMap.put(mgmg1, quotebuf);
						if(flag==0) {
							this.SendMessage(mgmg1 + " 저장됨 - " + polQuote);								
						}else if(flag==1) {
							this.SendMessage(mgmg1 + " 저장됨 - " + polQuote, Nickname);
						}
					}
				}else {
					if(flag==0) {
						this.SendMessage("입력 형식에 맞춰서 써주세용(~!넣을말 설명)");								
					}else if(flag==1) {
						this.SendMessage("입력 형식에 맞춰서 써주세용(~!넣을말 설명)",Nickname);
					}
					
				}
			}else if(cmcm.equals("!~")) {//output
				if(this.quoteMap.containsKey(mgmg.split("\\ ", 2)[0])) {
					Random R = new Random();
					String S = new String();
					quotebuf = this.quoteMap.get(mgmg.split("\\ ", 2)[0]);
					S = quotebuf.get(R.nextInt(quotebuf.size()));
					if(flag==0) {
						this.SendMessage(mgmg.split("\\ ", 2)[0] + " - " + S);								
					}else if(flag==1) {
						this.SendMessage(mgmg.split("\\ ", 2)[0] + " - " + S,Nickname);
					}
					
				}else {
					if(flag==0) {
						this.SendMessage("전 그런 거 몰라요(추가하려면 : ~!넣을말 설명)");
					}else if(flag==1) {
						this.SendMessage("전 그런 거 몰라요(추가하려면 : ~!넣을말 설명)",Nickname);
					}
					
				}
			}
			this.UpdateQuotes();
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		BotBrain A = new BotBrain();
		Scanner sc = new Scanner(System.in);
		String inp = new String();
		

		System.out.println("GagaBot Started. Hello World!");		
		A.InitSocket("1.255.101.144",8083);
		
		Timer Ptimer = new Timer(true);
		TimerTask Psend = new TimerTask() {
			@Override
			public void run() {
				A.SocketSend("P");
			}
		};
		Ptimer.schedule(Psend,0,240000 + (int)(Math.ceil(Math.random() * 40000)));
		
		A.initFiles();
		
		Thread listener = new Thread() {
			@Override
			public void run() {
				while(true) {
					A.SocketRecv();
				}
			}
		};
		listener.setDaemon(true);
		listener.start();
		
		
		while(true) {
			inp = sc.nextLine();
			if(inp.equals("exit")) {
				break;
			}else {
				A.SendMessage(inp);
			}
		}
		sc.close();
		
		A.CloseSocket();
		System.out.println("end");		        
    }
}
