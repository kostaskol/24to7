package gr.mapeu.a24to7_rebuild.Callbacks;


public interface LoginResponseHandler {
    void onLoginResponse(int code, String key, String user, String pass);
}
