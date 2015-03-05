/* Debug setting */
#define DEBUG FALSE

/* Enable Unicode */
#define UNICODE
#define _UNICODE

/* Target Windows Vista and above */
#define _WIN32_WINNT 0x0600

/* Includes */
#include <stdio.h>
#include <stdlib.h>
#include <winsock2.h>
#include <windows.h>
#include <shlwapi.h>

/* Application information */
#define APP_NAME L"ABCETray"
#define APP_VERSION "1.0"

/* Timers */
#define ID_TIMER1 42
#define SOCKET_TIMEOUT 2
#define TRAY_UPDATE_INTERVAL 10

/* Messages */
#define WM_TRAY WM_USER+1
#define MSG_TOGGLE WM_APP+1
#define MSG_EXIT WM_APP+2
UINT WM_TASKBARCREATED = 0;

/* Forward declarations */
LRESULT CALLBACK WindowProc(HWND, UINT, WPARAM, LPARAM);
int UpdateTray();

/* Global handles */
HINSTANCE global_hinst = NULL;
HWND global_hwnd = NULL;

/* Misc variables */
BOOL x64 = FALSE;
BOOL startup = TRUE;
/* Variables related to the system tray */
NOTIFYICONDATA tray;
HICON icon[3];
int icon_added = 0;

/* Variables related to controlling the ABCE */
BOOL alive = FALSE, starting = FALSE, stopping = FALSE;

/* Check if the ABCE is alive and answering requests */
int IsAlive() {
    SOCKET s;
    TIMEVAL timeout;
    struct sockaddr_in server;
    char *message, server_reply[32];
    int recv_size;
    fd_set write, err;
    DWORD iMode, iVal;

    /* Set parameters */
    timeout.tv_sec = SOCKET_TIMEOUT;
    timeout.tv_usec = 0;
    server.sin_addr.s_addr = inet_addr("127.0.0.1");
    server.sin_family = AF_INET;
    server.sin_port = htons(9300);

    /* Create a non-blocking TCP socket */
    s = socket(AF_INET, SOCK_STREAM, 0);
    if (s == INVALID_SOCKET)
	return alive;

    if (DEBUG) puts("Socket created.");

    iMode = 1;
    if (ioctlsocket(s, FIONBIO, &iMode) != NO_ERROR)
	return alive;

    if (DEBUG) puts("Socket now in non-blocking mode.");

    /* Set send and receive socket options */
    iVal = SOCKET_TIMEOUT*1000;
    if (setsockopt(s, SOL_SOCKET, SO_RCVTIMEO, (char *)&iVal, sizeof(DWORD)) == SOCKET_ERROR)
	return alive;
    if (setsockopt(s, SOL_SOCKET, SO_SNDTIMEO, (char *)&iVal, sizeof(DWORD)) == SOCKET_ERROR)
	return alive;

    if (DEBUG) puts("Socket send and receive timeouts set.");
    
    /* Connect to server */
    if (connect(s, (struct sockaddr *)&server, sizeof(server)) != SOCKET_ERROR)
        return alive;

    if (DEBUG) puts("Socket connect called.");

    /* Make the socket blocking again */
    iMode = 0;
    if (ioctlsocket(s, FIONBIO, &iMode) != NO_ERROR) {
	closesocket(s);
	return alive;
    }

    if (DEBUG) puts("Socket now in blocking mode.");

    /* Initialize file descriptors for the select call */
    FD_ZERO(&write);
    FD_ZERO(&err);
    FD_SET(s, &write);
    FD_SET(s, &err);

    if (DEBUG) puts("Attempting to connect.");

    /* Wait for the file descriptor to become available, i.e. connection succeeds */
    select(0, NULL, &write, &err, &timeout);

    if (FD_ISSET(s, &write)) {	
	/* Socket is connected. Set timeout values for send and receive */
	if (DEBUG) puts("Connection successful.");
	
	/* Send a GET request */
	message = "GET /idselect-user-service/alive/isAlive HTTP/1.0\r\n\r\n";
	if (send(s, message, strlen(message), 0) < 0) {
	    closesocket(s);
	    return alive;
	}
	if (DEBUG) puts("Message sent.");
	
	/* Receive a reply from the server */
	if ((recv_size = recv(s, server_reply, 31, 0)) == SOCKET_ERROR) {
	    closesocket(s);
	    return alive;
	}
	if (DEBUG) puts("Reply received.");
	
	/* Close the socket */
	closesocket(s);
	
	/* Add a NULL terminating character to make it a proper string before printing */
	server_reply[recv_size] = '\0';

	char *p = strstr(server_reply, "200 OK");
	if (p) {
	    /* Alive */
	    if (DEBUG) puts("ABCE is alive!");
	    return TRUE;
	} else {
	    /* Not alive */
	    if (DEBUG) puts("ABCE is dead!");
	    return FALSE;
	}
    } else {
	if (DEBUG) puts("Connection failed!");
	return FALSE;
    }

    return alive;
}

/* Stop the ABCE by graceful shutdown */
void StopABCE() {
    SHELLEXECUTEINFO execinfo;
    
    execinfo.cbSize = sizeof(SHELLEXECUTEINFO);
    execinfo.fMask = 0;
    execinfo.hwnd = NULL;
    execinfo.lpVerb = NULL;
    execinfo.lpFile = L"abce.cmd";
    execinfo.lpParameters = L"stop";
    execinfo.lpDirectory = L"%PROGRAMFILES%\\ABC4Trust\\User Client";
    execinfo.nShow = (DEBUG == TRUE ? SW_SHOWNORMAL : SW_HIDE);
    execinfo.hInstApp = NULL;
    ShellExecuteEx(&execinfo);
    stopping = TRUE;
    UpdateTray();
}

/* Start the ABCE */
void StartABCE() {
    SHELLEXECUTEINFO execinfo;

    if (alive == FALSE) {
	execinfo.cbSize = sizeof(SHELLEXECUTEINFO);
	execinfo.fMask = SEE_MASK_NOCLOSEPROCESS;
	execinfo.hwnd = NULL;
	execinfo.lpVerb = NULL;
	execinfo.lpFile = L"abce.cmd";
	execinfo.lpParameters = L"start";
	execinfo.lpDirectory = L"%PROGRAMFILES%\\ABC4Trust\\User Client";
	execinfo.nShow = (DEBUG == TRUE ? SW_SHOWNORMAL : SW_HIDE);
	execinfo.hInstApp = NULL;
	ShellExecuteEx(&execinfo);
	starting = TRUE;
	UpdateTray();
    }
}

int InitialiseTray() {
    /* Load icons */
    icon[0] = LoadImage(global_hinst, L"abce_disabled", IMAGE_ICON, 0, 0, LR_DEFAULTCOLOR);
    icon[1] = LoadImage(global_hinst, L"abce_enabled", IMAGE_ICON, 0, 0, LR_DEFAULTCOLOR);
    icon[2] = LoadImage(global_hinst, L"abce_working", IMAGE_ICON, 0, 0, LR_DEFAULTCOLOR);
    if (icon[0] == NULL || icon[1] == NULL || icon[2] == NULL) {
	/* This should never happen unless you compile with invalid icons in the resource file */
	return FALSE;
    }
  
    /* Populate NOTIFYICONDATA */
    tray.cbSize = sizeof(NOTIFYICONDATA);
    tray.uID = 0;
    tray.uFlags = NIF_MESSAGE|NIF_ICON|NIF_TIP;
    tray.hWnd = global_hwnd;
    tray.uCallbackMessage = WM_TRAY;
    tray.uTimeout = 15000;
    wcsncpy(tray.szTip, L"ABCE User Client", sizeof(tray.szTip)/sizeof(wchar_t));
    wcsncpy(tray.szInfoTitle, APP_NAME, sizeof(tray.szInfoTitle)/sizeof(wchar_t));
    tray.dwInfoFlags = NIIF_USER;
  
    /* Register TaskbarCreated to readd the tray icon if explorer.exe crashes */
    WM_TASKBARCREATED = RegisterWindowMessage(L"TaskbarCreated");

    /* Create a timer to start the ABCE after a short delay */
    SetTimer(global_hwnd, ID_TIMER1, 1000, (TIMERPROC) NULL);
  
    return TRUE;
}

int UpdateTray() {
    /* Update the tray icon */
    if (starting == TRUE || stopping == TRUE) {
	tray.hIcon = icon[2];
    } else {
	tray.hIcon = icon[alive == TRUE?1:0];
    }
  
    /* Try adding/modifying the icon until we succeed. This is due to a bug in Windows */
    while (Shell_NotifyIcon((icon_added?NIM_MODIFY:NIM_ADD), &tray) == FALSE) {
	Sleep(100);
    }
    icon_added = 1;

    return TRUE;
}

int RemoveTray() {
    if (!icon_added) {
	/* This should only happen if RemoveTray is called before UpdateTray */
	return FALSE;
    }
  
    if (Shell_NotifyIcon(NIM_DELETE, &tray) == FALSE) {
	return FALSE;
    }
  
    icon_added = 0;
    return TRUE;
}

void ShowContextMenu(HWND hwnd) {
    /* Create menu */
    LPCTSTR str;
    if (starting == TRUE)
	str = L"Starting";
    else if (stopping == TRUE)
	str = L"Stopping";
    else if (alive == TRUE)
	str = L"Stop";
    else
	str = L"Start";

    POINT point;
    HMENU menu = CreatePopupMenu();
    GetCursorPos(&point);
    InsertMenu(menu, -1, MF_BYPOSITION|(starting == TRUE || stopping == TRUE ? MF_DISABLED : 0), MSG_TOGGLE, str);
    InsertMenu(menu, -1, MF_BYPOSITION|MF_SEPARATOR, 0, NULL);
    InsertMenu(menu, -1, MF_BYPOSITION, MSG_EXIT, L"Exit");
  
    /* Track menu */
    SetForegroundWindow(hwnd);
    TrackPopupMenu(menu, TPM_BOTTOMALIGN, point.x, point.y, 0, hwnd, NULL);
    DestroyMenu(menu);
}

/* Program entry point */
int WINAPI WinMain(HINSTANCE hInst, HINSTANCE hPrevInstance, char *szCmdLine, int iCmdShow) {
    global_hinst = hInst;

    /* Check for 64-bit Windows */
    IsWow64Process(GetCurrentProcess(), &x64);

    /* Initialise the Winsock library */
    WSADATA wsa;
    if (WSAStartup(MAKEWORD(2,2),&wsa) != 0) {
	return 1;
    }

    /* Only run one instance of the program */
    HWND previnst = FindWindow(APP_NAME, NULL);
    if (previnst != NULL) {
	return 1;
    }

    /* Create window */
    WNDCLASSEX window = { sizeof(WNDCLASSEX), 0, WindowProc, 0, 0, hInst, NULL, NULL, (HBRUSH)(COLOR_WINDOW+1), NULL, APP_NAME, NULL };
    RegisterClassEx(&window);
    global_hwnd = CreateWindowEx(WS_EX_TOOLWINDOW|WS_EX_TOPMOST|WS_EX_LAYERED, window.lpszClassName, NULL, WS_POPUP, 0, 0, 0, 0, NULL, NULL, hInst, NULL);
    SetLayeredWindowAttributes(global_hwnd, 0, 1, LWA_ALPHA);

    /* Create the tray icon */
    InitialiseTray();
    UpdateTray();

    /* Message loop */
    MSG msg;
    while (GetMessage(&msg,NULL,0,0)) {
	TranslateMessage(&msg);
	DispatchMessage(&msg);
    }

    return msg.wParam;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam) {
    if (msg == WM_TRAY) {
	/* Show context menu on left click */
	if (lParam == WM_RBUTTONDOWN) {
	    ShowContextMenu(hwnd);
	}
    }
    /* Update the tray icon when the taskbar is created */
    else if (msg == WM_TASKBARCREATED) {
	icon_added = 0;
	UpdateTray();
    }
    /* Handle commands from the popup menu */
    else if (msg == WM_COMMAND) {
	int wmId=LOWORD(wParam);
	if (wmId == MSG_TOGGLE) {
	    if (alive == TRUE) {
		StopABCE();
	    } else {
		StartABCE();
	    }
	}
	else if (wmId == MSG_EXIT) {
	    DestroyWindow(hwnd);
	}
    }
    /* Handle the timer to start the ABCE */
    else if (msg == WM_TIMER) {
	if (wParam == ID_TIMER1) {
	    if (startup) {
		startup = FALSE;
		/* Re-create the timer to change the interval */
		KillTimer(global_hwnd, ID_TIMER1);
		SetTimer(global_hwnd, ID_TIMER1, TRAY_UPDATE_INTERVAL*1000, (TIMERPROC)NULL);
		StartABCE();
	    } else {
		BOOL prev_alive = alive;
		alive = IsAlive();
		if (prev_alive != alive) {
		    starting = stopping = FALSE;
		}
		UpdateTray();
	    }
	}
    }
    /* Handle shutdown message */
    else if (msg == WM_QUERYENDSESSION) {
	if (alive == TRUE)
	    StopABCE();
	WSACleanup();
	PostQuitMessage(0);
    }
    /* Handle window close message */
    else if (msg == WM_DESTROY) {
	if (alive == TRUE)
	    StopABCE();
	RemoveTray();
	WSACleanup();
	PostQuitMessage(0);
    }

    return DefWindowProc(hwnd, msg, wParam, lParam);
}
