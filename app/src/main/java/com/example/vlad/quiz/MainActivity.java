package com.example.vlad.quiz;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnFragmentInteractionListener {
    public static final String CHOICES = "pref_numberOfChoices";

    private static final String KEY_QUIZ = "key_quiz";
    private static final String KEY_IS_CALL = "key_isCall";
    private static final String KEY_IS_CHEAT = "key_isCheat";

    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION = 101;

    private ViewPager viewPager;
    private FragmentPagerAdapter fragmentPagerAdapter;

    private Quiz quiz;
    private boolean isCheat = false;
    private boolean isCall = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int numAnswers = Integer.valueOf(sharedPreferences.getString(CHOICES, null));

        if (quiz == null || quiz.getResult() == 10) quiz = new Quiz(this, numAnswers);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Quiz.Question question = quiz.getQuestion(position);
                return MainActivityFragment.newInstance(position, quiz.getQuestionsNum(), question.photoPath, question.correctAnswer, question.answers);
            }

            @Override
            public int getCount() {
                return quiz.getQuestionsNum();
            }
        };
        viewPager.setAdapter(fragmentPagerAdapter);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        quiz = savedInstanceState.getParcelable(KEY_QUIZ);
        isCheat = savedInstanceState.getBoolean(KEY_IS_CHEAT);
        isCall = savedInstanceState.getBoolean(KEY_IS_CALL);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_QUIZ, quiz);
        outState.putBoolean(KEY_IS_CHEAT, isCheat);
        outState.putBoolean(KEY_IS_CALL, isCall);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (isCheat) {
            menu.findItem(R.id.action_cheat).setEnabled(false);
        }
        if (isCall) {
            menu.findItem(R.id.action_phone).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_phone:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_STORAGE_PERMISSION);
                }
                else {
                        dispatchSharePictureIntent();
                        item.setEnabled(false);
                        isCall = true;
                }
                return true;
            case R.id.action_settings:
                item.setEnabled(false);
                dispatchPreferencesIntent();
                return true;
            case R.id.action_cheat:
                // current fragment
                MainActivityFragment fragment =
                        (MainActivityFragment) getSupportFragmentManager()
                                .findFragmentByTag("android:switcher:" +
                                        R.id.viewPager +
                                        ":" +
                                        viewPager.getCurrentItem());
                fragment.cheat();
                item.setEnabled(false);
                isCheat = true;
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void registerGuess(Boolean isCorrect) {
        quiz.registerGuess(isCorrect);
    }

    @Override
    public void nextQuestion() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        }, 300);
    }

    private void dispatchPreferencesIntent() {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
    }

    private void dispatchSharePictureIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        String path = takeScreenshot();
        if (path != null) {
            Uri uri = Uri.parse(path);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/jpeg");
            startActivity(shareIntent);
        }
    }

    private String takeScreenshot() {
        final String name = "quiz_" + System.currentTimeMillis() + ".jpg";
        View view = getWindow().getDecorView().getRootView();
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        String location = MediaStore.Images.Media.insertImage(
                this.getContentResolver(), bitmap, name,
                name);
        return location;
    }
}
