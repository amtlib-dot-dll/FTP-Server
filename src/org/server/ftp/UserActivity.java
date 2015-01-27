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

import static org.server.ftp.Constants.DEFAULT_ROOT;
import static org.server.ftp.Constants.EXTRA_NAME;

public class UserActivity extends Activity {
    public static final int REQUEST_CODE = UserActivity.class.hashCode();
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
            try {
                User user = userManager.getUserByName(this.username);
                root.setText(user.getHomeDirectory());
                enabled.setChecked(user.getEnabled());
                readonly.setChecked(user.authorize(new WriteRequest()) == null);
            } catch (FtpException e) {
                e.printStackTrace();
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
//                Intent intent = new Intent();
//                intent.putExtra(EXTRA_NAME, getIntent().getStringExtra(EXTRA_NAME));
//                if (password.getText().length() > 0) {
//                    intent.putExtra(EXTRA_PASSWORD, password.getText().toString());
//                }
//                if (root.getText().length() > 0) {
//                    intent.putExtra(EXTRA_ROOT, root.getText().toString());
//                }
//                intent.putExtra(EXTRA_ENABLED, enabled.isChecked());
//                intent.putExtra(EXTRA_READONLY, readonly.isChecked());
//                setResult(RESULT_OK, intent);
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

                    //todo
                    authorities.add(new ConcurrentLoginPermission(0, 0));
                    authorities.add(new TransferRatePermission(0, 0));

                    user.setAuthorities(authorities);
                    userManager.save(user);
                    setResult(RESULT_OK);
                } catch (FtpException e) {
                    setResult(RESULT_CANCELED);
                    e.printStackTrace();
                }
                finish();
                return true;
            case R.id.delete:
                try {
                    userManager.delete(username);
                    setResult(RESULT_OK);
                } catch (FtpException e) {
                    setResult(RESULT_CANCELED);
                    e.printStackTrace();
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}