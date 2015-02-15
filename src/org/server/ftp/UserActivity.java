package org.server.ftp;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.usermanager.impl.*;

import java.util.ArrayList;
import java.util.List;

import static org.server.ftp.Constants.*;

public class UserActivity extends Activity {
    private UserManager userManager;
    private String username;
    private EditText password;
    private EditText root;
    private CheckBox enabled;
    private CheckBox readonly;
    private EditText name;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userManager = ((ServerApplication) getApplication()).getUserManager();
        if (getActionBar() == null) {
            throw new NullPointerException();
        }
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.user);
        username = getIntent().getStringExtra(EXTRA_NAME);
        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);
        root = (EditText) findViewById(R.id.root);
        enabled = (CheckBox) findViewById(R.id.enabled);
        readonly = (CheckBox) findViewById(R.id.readonly);
        if (username == null) {
            root.setText(DEFAULT_ROOT);
        } else {
            name.setInputType(InputType.TYPE_NULL);
            name.setText(username);
            try {
                User user = userManager.getUserByName(username);
                root.setText(user.getHomeDirectory());
                enabled.setChecked(user.getEnabled());
                readonly.setChecked(user.authorize(new WriteRequest()) == null);
            } catch (FtpException e) {
                throw new IllegalStateException();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage, menu);
        if (username == null) {
            menu.findItem(R.id.delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.cancel:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.save:
                try {
                    BaseUser user;
                    if (username == null) {
                        user = new BaseUser();
                        user.setName(name.getText().toString());
                    } else {
                        user = (BaseUser) userManager.getUserByName(username);
                    }
                    if (password.getText().length() > 0) {
                        user.setPassword(password.getText().toString());
                    }
                    user.setEnabled(enabled.isChecked());
                    user.setHomeDirectory(root.getText().toString());
                    List<Authority> authorities = new ArrayList<>(3);
                    if (!readonly.isChecked()) {
                        authorities.add(new WritePermission());
                    }

                    authorities.add(new ConcurrentLoginPermission(0, 0));
                    authorities.add(new TransferRatePermission(0, 0));

                    user.setAuthorities(authorities);
                    userManager.save(user);
                    setResult(RESULT_OK);
                } catch (FtpException e) {
                    setResult(RESULT_CANCELED);
                }
                finish();
                return true;
            case R.id.delete:
                try {
                    userManager.delete(username);
                    setResult(RESULT_OK);
                } catch (FtpException e) {
                    setResult(RESULT_CANCELED);
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}