package com.mcbath.rebecca.gimbalbeacondemo;

import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.GimbalDebugger;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;
import com.mcbath.rebecca.gimbalbeacondemo.DAO.GimbalDAO;
import com.mcbath.rebecca.gimbalbeacondemo.Models.GimbalEvent;
import com.mcbath.rebecca.gimbalbeacondemo.UI.MainActivity;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Rebecca McBath
 * on 2019-08-02.
 */
public class GimbalIntegration {

	private static final String GIMBAL_APP_API_KEY = "c65f52af-8556-40a6-ad69-f3536ab227dd";
	private static final Boolean ENABLE_PUSH_MESSAGING = null;

	private static final int MAX_NUM_EVENTS = 100;

	private Application app;
	private Context appContext;
	private LinkedList<GimbalEvent> events;
	private PlaceEventListener placeEventListener;
	private CommunicationListener communicationListener;
	private static GimbalIntegration instance;

	public static GimbalIntegration init(Application app) {
		if (instance == null) {
			instance = new GimbalIntegration(app);
		}
		return instance;
	}

	public static GimbalIntegration instance() {
		if (instance == null) {
			throw new IllegalStateException("Gimbal integration not initialized from Application");
		}
		return instance;
	}

	private GimbalIntegration(Application app) {
		this.app = app;
		this.appContext = app.getApplicationContext();
	}

	public void onCreate() {
		Gimbal.setApiKey(app, GIMBAL_APP_API_KEY);

		GimbalDebugger.enableBeaconSightingsLogging();
		GimbalDebugger.enablePlaceLogging();

		if (ENABLE_PUSH_MESSAGING != null) {
			// Only needs to be enabled once per app instance.  Additional calls will have no effect.
			CommunicationManager.getInstance().enablePushMessaging(ENABLE_PUSH_MESSAGING);
		}

		events = new LinkedList<>(GimbalDAO.getEvents(app));

		// Setup PlaceEventListener for beacon events
		placeEventListener = new PlaceEventListener() {

			@Override
			public void onVisitStart(Visit visit) {
				addEvent(new GimbalEvent(GimbalEvent.TYPE.PLACE_ENTER, visit.getPlace().getName(),
						new Date(visit.getArrivalTimeInMillis())));
			}

			@Override
			public void onVisitStartWithDelay(Visit visit, int delayTimeInSeconds) {
				if (delayTimeInSeconds > 0) {
					addEvent(new GimbalEvent(GimbalEvent.TYPE.PLACE_ENTER_DELAY, visit.getPlace().getName(),
							new Date(System.currentTimeMillis())));
				}
			}

			@Override
			public void onVisitEnd(Visit visit) {
				addEvent(new GimbalEvent(GimbalEvent.TYPE.PLACE_EXIT, visit.getPlace().getName(),
						new Date(visit.getDepartureTimeInMillis())));
			}
		};

		PlaceManager.getInstance().addListener(placeEventListener);

		// Setup CommunicationListener
		communicationListener = new CommunicationListener() {
			@Override
			public Notification.Builder prepareCommunicationForDisplay(Communication communication,
			                                                           Visit visit, int notificationId) {
				addEvent(new GimbalEvent(GimbalEvent.TYPE.COMMUNICATION_PRESENTED,
						communication.getTitle() + ":  CONTENT_DELIVERED", new Date()));

				// If you want a custom notification create and return it here
				return null;
			}

			@Override
			public Notification.Builder prepareCommunicationForDisplay(Communication communication,
			                                                           Push push, int notificationId) {
				addEvent(new GimbalEvent(GimbalEvent.TYPE.COMMUNICATION_INSTANT_PUSH,
						communication.getTitle() + ":  CONTENT_DELIVERED", new Date()));
				// communication.getAttributes()

				// If you want a custom notification create and return it here
				return null;
			}

			@Override
			public void onNotificationClicked(List<Communication> communications) {
				for (Communication communication : communications) {
					if(communication != null) {
						addEvent(new GimbalEvent(GimbalEvent.TYPE.NOTIFICATION_CLICKED,
								communication.getTitle() + ": CONTENT_CLICKED", new Date()));
						Intent intent  = new Intent(appContext, MainActivity.class);
						intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
						appContext.startActivity(intent);
					}
				}
			}
		};

		CommunicationManager.getInstance().addListener(communicationListener);
	}

	private void addEvent(GimbalEvent event) {
		while (events.size() >= MAX_NUM_EVENTS) {
			events.removeLast();
		}
		events.add(0, event);
		GimbalDAO.setEvents(appContext, events);
	}

	public void onTerminate() {
		PlaceManager.getInstance().removeListener(placeEventListener);
		CommunicationManager.getInstance().removeListener(communicationListener);
	}
}
