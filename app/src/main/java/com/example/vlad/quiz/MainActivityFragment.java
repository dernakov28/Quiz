package com.example.vlad.quiz;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivityFragment extends Fragment {
    private OnFragmentInteractionListener listener;

    public static final String PHOTO_PATH = "photoPath";
    public static final String ANSWERS = "answers";
    public static final String QUESTION_NUMBER = "questionNumber";
    public static final String QUESTIONS_NUMBER = "questionsNumber";
    public static final String CORRECT_ANSWER = "correctAnswer";
    public static final String GUESS = "guess";

    private TextView questionNumberTextView;
    private ImageView photoImageView;
    private ArrayList<Button> guessButtons;

    private ArrayList<String> answers;
    private  String correctAnswer;

    private String guess;

    public static MainActivityFragment newInstance(int questionNumber, int questionsNumber, String photoPath, String correctAnswer, ArrayList<String> answers) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putString(PHOTO_PATH, photoPath);
        args.putString(CORRECT_ANSWER, correctAnswer);
        args.putStringArrayList(ANSWERS, answers);
        args.putInt(QUESTION_NUMBER, questionNumber);
        args.putInt(QUESTIONS_NUMBER, questionsNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public MainActivityFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            listener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            guess = savedInstanceState.getString(GUESS);
        }

        Bundle args = getArguments();
        correctAnswer = args.getString(CORRECT_ANSWER);
        answers = args.getStringArrayList(ANSWERS);
        Collections.shuffle(answers);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(GUESS, guess);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        questionNumberTextView = view.findViewById(R.id.questionNumberTextView);

        Bundle args = getArguments();

        String photoPath = args.getString(PHOTO_PATH);

        int questionNumber = args.getInt(QUESTION_NUMBER);
        int questionsNumber = args.getInt(QUESTIONS_NUMBER);

        questionNumberTextView.setText(getString(R.string.question, questionNumber + 1, questionsNumber));

        photoImageView = view.findViewById(R.id.photoImageView);
        Drawable photo = null;
        try {
            photo = Drawable.createFromStream(getActivity().getAssets().open(photoPath), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        photoImageView.setImageDrawable(photo);

        LinearLayout[] answersRows = new LinearLayout[2];
        answersRows[0] = view.findViewById(R.id.answersRow1);
        answersRows[1] = view.findViewById(R.id.answersRow2);

        guessButtons = new ArrayList<>();
        for (LinearLayout row : answersRows) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                guessButtons.add(button);
            }
        }
        int answerIndex = 0;
        for (final Button guessButton : guessButtons) {
            if (answerIndex > answers.size() - 1) {
                guessButton.setVisibility(View.GONE);
                answerIndex++;
                continue;
            }
            guessButton.setText(answers.get(answerIndex));
            if (guess == null) {
                guessButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button button = (Button) view;
                        Drawable buttonBackground = button.getBackground();

                        String userGuess = button.getText().toString();
                        guess = userGuess;
                        listener.registerGuess(userGuess.equals(correctAnswer));

                        if (userGuess.equals(correctAnswer)) {
                            buttonBackground.setColorFilter(getResources().getColor(R.color.correctAnswer), PorterDuff.Mode.MULTIPLY);
                        }
                        else {
                            buttonBackground.setColorFilter(getResources().getColor(R.color.incorrectAnswer), PorterDuff.Mode.MULTIPLY);
                        }

                        for (Button guessButton : guessButtons) {
                            if (guessButton.getText().toString().equals(correctAnswer)) {
                                buttonBackground = guessButton.getBackground();
                                buttonBackground.setColorFilter(getResources().getColor(R.color.correctAnswer), PorterDuff.Mode.MULTIPLY);
                            }
                            guessButton.setClickable(false);
                        }

                        listener.nextQuestion();
                    }
                });
            }
            else {
                guessButton.setClickable(false);
                if (answers.get(answerIndex).equals(guess)) {
                    Drawable buttonBackground = guessButton.getBackground();
                    buttonBackground.setColorFilter(guess.equals(correctAnswer) ? getResources().getColor(R.color.correctAnswer) : getResources().getColor(R.color.incorrectAnswer), PorterDuff.Mode.MULTIPLY);
                }
                if (answers.get(answerIndex).equals(correctAnswer)) {
                    Drawable buttonBackground = guessButton.getBackground();
                    buttonBackground.setColorFilter(getResources().getColor(R.color.correctAnswer), PorterDuff.Mode.MULTIPLY);
                }
            }
            answerIndex++;
        }
        return view;
    }

    public interface OnFragmentInteractionListener {
        void registerGuess(Boolean isCorrect);
        void nextQuestion();
    }

    public void cheat() {
        int removeButtons = 2;
        for (Button btn : guessButtons) {
            if (!btn.getText().toString().equals(correctAnswer) && removeButtons > 0) {
                btn.setVisibility(View.GONE);
                removeButtons--;
            }
        }
    }

    public String toString() {
        return "Fragment: " + correctAnswer;
    }

}
