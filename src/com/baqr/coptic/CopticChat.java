package com.baqr.coptic;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.baqr.baqr.R;
import com.baqr.database.MessageData;
import com.baqr.database.MulticastData;
import com.baqr.database.MulticastDatabaseHandler;
import com.baqr.multicast.XMLParser;
import com.baqr.multicast.discovery.Discovery;
import com.baqr.multicast.discovery.DiscoveryException;
import com.baqr.multicast.discovery.DiscoveryListener;
import com.baqr.multicast.transmitter.GenerateCoT;
import com.baqr.multicast.transmitter.Transmitter;
import com.baqr.multicast.transmitter.TransmitterException;

public class CopticChat extends Fragment implements DiscoveryListener, OnEditorActionListener, OnClickListener  {
    
	// XML Node Keys
	static final String KEY_EVENT = "event"; // parent node
	static final String KEY_POINT = "point";
	static final String KEY_DETAIL = "detail";
	static final String KEY_LINK = "link";
	static final String KEY_REMARKS = "remarks";
	
	private static final String EXTRA_MESSAGE = "message";
	private static final long ONE_SECOND = 1000L;
	private static final String DEFAULT_CMD = "Location Update";
	protected static final int REQUEST_OK = 1;
	private static final int DISABLED = 100;
	private static final String MULTI_CMD = "0";

    private TextView chatView;
    private EditText inputView;
    private Button sendButton;    
    private Context context;
    private Discovery discovery;
    private Transmitter transmitter;
    private boolean discoveryStarted;
    private SharedPreferences preferences;
    	
	// Set up layout
	private Button btnPreset1;
	private Button btnPreset2;
	private Button btnPreset3;
	private Button btnPreset4;
	private Button btnPreset5;
	private Button btnPreset6;
	private Button btnPreset7;
	
	// Preference commands
	private String cmd1 = null;
	private String cmd2 = null;
	private String cmd3 = null;
	private String cmd4 = null;
	private String cmd5 = null;
	private String cmd6 = null;
	
	// Received message variables
	private static String rId = null;
	private static String rLat = null;
	private static String rLon = null;
	private static String rMsg = null;
	private static String rTime = null;
	private static String rRoom = null;
	
	// Sending message variables
	private String message = null;
	private int interval = 100;
	private String uid = null;
	private String room = null;
	
	// Timer variables
	private static TimerTask mTimerTask;
	private final Handler handler = new Handler();
	private static Timer myTimer = new Timer();
	private MulticastDatabaseHandler dbHandler;
	
	/**
	 * Constructor
	 */
	public CopticChat(Context ctx) {
		context = ctx;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.baqrmulticast_send_data,container,false);

        discovery = new Discovery();
        discovery.setDisoveryListener(this);
        transmitter = new Transmitter();

        chatView = (TextView) rootView.findViewById(R.id.chat);

        inputView = (EditText) rootView.findViewById(R.id.input);
        inputView.setOnEditorActionListener(this);

        sendButton = (Button) rootView.findViewById(R.id.send);
        sendButton.setOnClickListener(this);
        
    	// Set up the layout
    	btnPreset1 = (Button) rootView.findViewById(R.id.btnPreset9);
    	btnPreset2 = (Button) rootView.findViewById(R.id.btnPreset10);
    	btnPreset3 = (Button) rootView.findViewById(R.id.btnPreset11);
    	btnPreset4 = (Button) rootView.findViewById(R.id.btnPreset12);
    	btnPreset5 = (Button) rootView.findViewById(R.id.btnPreset13);
    	btnPreset6 = (Button) rootView.findViewById(R.id.btnPreset14);
    	btnPreset7 = (Button) rootView.findViewById(R.id.btnPreset15);
    	
    	
    	// Get preferences
    	preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	cmd1 = preferences.getString("cmd_9", DEFAULT_CMD);
    	cmd2 = preferences.getString("cmd_10", DEFAULT_CMD);
    	cmd3 = preferences.getString("cmd_11", DEFAULT_CMD);
    	cmd4 = preferences.getString("cmd_12", DEFAULT_CMD);
    	cmd5 = preferences.getString("cmd_13", DEFAULT_CMD);
    	cmd6 = preferences.getString("cmd_14", DEFAULT_CMD);
    	
    	interval = preferences.getInt("sendInterval", DISABLED);
    	uid = preferences.getString("multicastName", "BAQR-001");
    	room = preferences.getString("chatRoomName", "All Chat Rooms");
    	
    	// Send background location updates if enabled
    	if (interval != DISABLED) {
    		doTimerTask();
    	}
    	
    	btnPreset1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd1 != null | cmd1 != MULTI_CMD) {
					sendChatMessage(cmd1);
				}
			}
		});
    	
    	btnPreset2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd2 != null | cmd2 != MULTI_CMD) {
					sendChatMessage(cmd2);
				}		
			}
		}); 
    	
    	btnPreset3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd3 != null | cmd3 != MULTI_CMD) {
					sendChatMessage(cmd3);				
				}
			}
		});
    	
    	btnPreset4.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd4 != null | cmd4 != MULTI_CMD) {
					sendChatMessage(cmd4);				
				}		
			}
		});
    	
    	btnPreset5.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd5 != null | cmd5 != MULTI_CMD) {
					sendChatMessage(cmd5);				
				}				
			}
		});
    	
    	btnPreset6.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (cmd6 != null | cmd6 != MULTI_CMD) {
					sendChatMessage(cmd6);				
				}
			}
		});
    	
    	btnPreset7.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				chatView.setText("");
				dbHandler = new MulticastDatabaseHandler(context);
				dbHandler.Delete_All_Messages();
				dbHandler.close();
			}
		});
    	
    	sendButton.setOnLongClickListener(new View.OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				 Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		         i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
		         try {
		             startActivityForResult(i, REQUEST_OK);
		         } catch (Exception e) {
		        	 	Toast.makeText(context, "Error initializing speech engine.", Toast.LENGTH_LONG).show();
		         }
				return false;
			}
		});
        
        return rootView;
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        if (requestCode == REQUEST_OK  && resultCode == Activity.RESULT_OK) {
	        		ArrayList<String> thingsYouSaid = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	        		inputView.setText(thingsYouSaid.get(0));
	        }
	}

    @Override
    public void onResume() {
        super.onResume();

        try {
            discovery.enable();
            discoveryStarted = true;
        } catch (DiscoveryException exception) {
            appendChatMessage("* (!) Could not start discovery: " + exception.getMessage());
            discoveryStarted = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (discoveryStarted) {
            discovery.disable();
        }
    }

    private void appendChatMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                chatView.append(message + "\n");
            }
        });
    }
    
	/**
	 * Function to take the received XML and sends to the XML parser
	 */
    private void appendChatMessageFromSender(String sender, String message) {
    	try {
    		System.out.println(message);
			ParseMessage(sender, message);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
    }

    @Override
    public void onDiscoveryError(Exception exception) {
        appendChatMessage("* (!) Discovery error: " + exception.getMessage());
    }

    @Override
    public void onDiscoveryStarted() {
        appendChatMessage("* (>) Discovery started");
    }

    @Override
    public void onDiscoveryStopped() {
        appendChatMessage("* (<) Discovery stopped");
    }

    @Override
    public void onIntentDiscovered(InetAddress address, Intent intent) {
        if (!intent.hasExtra(EXTRA_MESSAGE)) {
            appendChatMessage("* (!) Received Intent without message");
            return;
        }

        String message = intent.getStringExtra(EXTRA_MESSAGE);
        String sender  = address.getHostName();
        
        appendChatMessageFromSender(sender, message);
    }

    @Override
    public void onClick(View v) {
        sendChatMessage(null);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            sendChatMessage(null);
            return true;
        }

        return false;
    }
    
	/**
	 * Function generate CoT string for transmission
	 */
    public void sendChatMessage(String msg) {
    	
    	message = null;
    	
    	if (msg != null) {
    		message = msg;
    	} else {
    		message = inputView.getText().toString();
    	}

        if (message.length() == 0) {
        	Toast.makeText(context, "No message", Toast.LENGTH_SHORT).show();
            return; // No message to send
        }
        
        // Generate our CoT string
        String buildCoT = GenerateCoT.GenerateChat(message, context);
        		
        inputView.setText("");
        
        // Send CoT string as Intent, and transmit
        Intent intent = new Intent();
        intent.putExtra(EXTRA_MESSAGE, buildCoT);

        transmitIntentOnBackgroundThread(intent);
        
    }

    private void transmitIntentOnBackgroundThread(final Intent intent) {
        new Thread() {
            public void run() {
                transmitIntent(intent); 
            }
        }.start();
    }

    private void transmitIntent(final Intent intent) {
        try {
            transmitter.transmit(intent);
        } catch (TransmitterException exception) {
            appendChatMessage("Could not transmit intent: " + exception.getMessage());
        }
    }
    
    public Document GetDomElement(String xml) {
		
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        try {
 
            DocumentBuilder db = dbf.newDocumentBuilder();
 
            InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is); 
 
        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }
        // return DOM
        return doc;
    }
	
	public String getValue(Element item, String str) {      
	    NodeList n = item.getElementsByTagName(str);        
	    return this.getElementValue(n.item(0));
	}
	 
	public final String getElementValue( Node elem ) {
		Node child;
		
    	if( elem != null){
    		if (elem.hasChildNodes()){
                for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                    if( child.getNodeType() == Node.TEXT_NODE  ){
                        return child.getNodeValue();
                    }
                }
            }
        }
    	
    	return "";
	} 
	
	public void ParseMessage(String sendAddr, String msg) throws XmlPullParserException, IOException {
		XMLParser parser = new XMLParser();
		Document doc = parser.getDomElement(String.valueOf(msg)); // getting DOM element
		doc.getDocumentElement().normalize();
		
		System.out.println(msg);
				
		String[] mList = { KEY_EVENT, KEY_POINT, KEY_DETAIL, KEY_LINK, KEY_REMARKS };
		
		for (int ix = 0; ix < mList.length; ix++) {
			NodeList nList = doc.getElementsByTagName(mList[ix]);
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				 
				Node nNode = nList.item(temp);
		 		 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		 
					Element eElement = (Element) nNode;
					
					try {
						switch (ix) {
						case 0:
							// Parse UID
							String temp1 = eElement.getAttribute("uid");
							String[] parseID = temp1.split("[.]");
							rId = parseID[1];
							rRoom = parseID[2];
							
							System.out.println(rId);
							System.out.println(rRoom);
							
							// Parse Time
							String temp2 = eElement.getAttribute("time");
							String [] parseTime = temp2.split("");
							String hour = parseTime[12];
							String hour2 = parseTime[13];
							String min = parseTime[15];
							String min2 = parseTime[16];
							rTime = hour + hour2 + ":" + min + min2;
							System.out.println(rTime);
							break;
						case 1:
							// Parse Latitude and Longitude
							rLat = eElement.getAttribute("lat");
							rLon = eElement.getAttribute("lon");
							System.out.println(rLat + " " + rLon);
							break;
						case 4:
							// Parse Message
							rMsg = nNode.getFirstChild().getNodeValue();
							System.out.println(rMsg);
							break;
						default:
							break;
						}
						
					} catch (Exception e) {
						Log.e("Failed to parse XML", "" + e);
					}
				}
			}
		}
		
		/* 
		 * Avoid our periodic updates from being listed in the list view
		 * and check the chat room name. If all criteria is met, then append
		 * to the list view, and update the database
		 */
			if (rRoom.equals(room) | rRoom.equals("All Chat Rooms")) {
				appendChatMessage(rTime + " " + "<" + rId + "> " + rMsg);
				
				dbHandler = new MulticastDatabaseHandler(context);
				dbHandler.Add_Message(new MulticastData(rId, rLat, rLon, "0", rMsg, rTime));
				dbHandler.close();
			}
		}
	
	/**
	 * Function to start CoT background updates. 
	 */
	public void doTimerTask(){
		 
		mTimerTask = new TimerTask() {
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						String buildCoT = GenerateCoT.GenerateChat(DEFAULT_CMD, context);
						
				        Intent intent = new Intent();
				        intent.putExtra(EXTRA_MESSAGE, buildCoT);

				        transmitIntentOnBackgroundThread(intent);
					}
    	        });
    	    }};
 
        // public void schedule (TimerTask task, long delay, long period) 
    	myTimer.schedule(mTimerTask, ONE_SECOND, interval);  // 
	}
	
	/**
	 * Function stop CoT transmissions
	 */
	public static void stopTask() {
		// Cancel and purge time when done
 	   	if(myTimer !=null){
 	   		myTimer.cancel();
 	   		myTimer.purge();
 	   	}
	}
}

