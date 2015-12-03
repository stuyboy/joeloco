package com.joechang.loco.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.joechang.loco.R;
import com.joechang.kursor.AndroidCommandLineProcessor;
import com.joechang.kursor.CommandLineProcessor;
import com.joechang.kursor.CliOutputWriter;
import com.joechang.loco.utils.UserInfoStore;

import java.net.URL;

/**
 * Author:    joechang
 * Created:   8/13/15 1:32 PM
 * Purpose:   A quick fragment for interacting with the command line processor.
 */
public class CommandLineFragment extends ChatFragment implements CliOutputWriter {

    private CommandLineProcessor clp;
    private String commandLinePersonaName;

    public static CommandLineFragment newInstance() {
        CommandLineFragment cf = new CommandLineFragment();
        cf.setShowFlingBar(false);
        return cf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        clp = new AndroidCommandLineProcessor(getActivity(), this);
        commandLinePersonaName = getActivity().getString(R.string.commandLinePersona);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void sendMessage() {
        String whatwasMessage = getMessage();
        super.sendMessage();

        //Now process the message, bro.
        clp.processMessage("Kursor", UserInfoStore.getInstance(getActivity()).getUserId(), whatwasMessage);
    }

    @Override
    public void outputCommandResponse(String[] destination, String[] resps) {
        for (String response : resps) {
            persistMessage(
                    commandLinePersonaName,
                    commandLinePersonaName,
                    response
            );
        }
    }

    @Override
    public void outputNotFound(String[] destination, String message) {
        persistMessage(
                commandLinePersonaName,
                commandLinePersonaName,
                message
        );
    }

    @Override
    public void outputCommandResponse(String[] destinations, URL resource) {
        outputCommandResponse(destinations, new String[] { resource.toString() });
    }
}
