package com.angelbroking.smartapi.ticker;

import com.angelbroking.smartapi.http.exceptions.SmartAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnErrorImp implements OnError {

    private static final String ERROR = "Error: ";
    private static final Logger logger = LoggerFactory.getLogger(OnErrorImp.class);

    @Override
    public void onError(Exception exception) {
        logger.error(ERROR + exception.getMessage());
    }

    @Override
    public void onError(SmartAPIException smartAPIException) {
        logger.error(ERROR + smartAPIException.getMessage());
    }

    @Override
    public void onError(String error) {
        logger.error(ERROR + error);

    }
}
