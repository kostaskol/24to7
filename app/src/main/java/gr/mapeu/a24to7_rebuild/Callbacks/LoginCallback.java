package gr.mapeu.a24to7_rebuild.Callbacks;


public interface LoginCallback {
    void loginHandler(int code, String key);
    void logoutHandler(int code);
}
