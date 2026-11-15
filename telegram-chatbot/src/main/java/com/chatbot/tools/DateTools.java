package com.chatbot.tools;

import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateTools {


    @Tool("Gibt das heutige Datum zurück")
    public String today() {
        return LocalDate.now().toString();
    }

    @Tool("Berechnet, wie viele Tage es bis zu einem Datum (YYYY-MM-DD) sind. "
            + "Eine negative Zahl bedeutet: Das Datum liegt in der Vergangenheit.")
    public long daysUntil(String date) {
        return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date));
    }

}
