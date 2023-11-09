package ru.chio;

public class InMemoryAuthencticationProvider implements AuthenticationProvider{
    @Override
    public String getUsernameByLoginAndPassword(String login, String password) {
        return null;
    }

    @Override
    public boolean register(String login, String password, String username) {
        return false;
    }
}
