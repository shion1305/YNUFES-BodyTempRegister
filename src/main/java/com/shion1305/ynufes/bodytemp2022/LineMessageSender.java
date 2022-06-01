package com.shion1305.ynufes.bodytemp2022;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.Broadcast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.FlexComponent;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.unit.FlexFontSize;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.model.message.flex.unit.FlexMarginSize;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class LineMessageSender {
    private static final Logger logger = Logger.getLogger("MessageSender");
    private final LineMessagingClient client;

    public LineMessageSender(String token) {
        this.client = LineMessagingClient.builder(token).build();
    }

    private void broadcast(Message message) {
        try {
            client.broadcast(new Broadcast(message)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void reply(ReplyMessage message) {
        try {
            client.replyMessage(message).get(30, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            logger.severe("ERROR in sending Message");
            e.printStackTrace();
        }
    }

    private void push(PushMessage message) {
        client.pushMessage(message);
    }

    public enum ErrorType {InvalidFormat, NAME_NOT_FOUND, INVALID_TEMP_FORMAT, SERVER_ERROR, ERROR_UNKNOWN}

    public void sendWelcomeMessage(String token) {
        Text title = createLabel("体温入力BOTへようこそ", true);
        Text description = createLabel("まずはじめに\nSpreadSheet上に登録されているあなたの名前を入力してください。", false);
        Box mainBox = Box.builder().layout(FlexLayout.VERTICAL)
                .contents(title, description).spacing(FlexMarginSize.SM).build();
        reply(new ReplyMessage(token, FlexMessage.builder()
                .contents(Bubble.builder().body(mainBox).build())
                .altText("体温入力BOTへようこそ").build()));
    }

    public void nameSuccess(String name, String token) {
        Text title = createLabel("名前を登録しました", true);
        Text description = createLabel("以下の内容で登録しました", false);
        Map<String, String> data = new HashMap<>();
        data.put("名前", name);
        Box mainBox = Box.builder().layout(FlexLayout.VERTICAL)
                .contents(title, description, listItems(data)).spacing(FlexMarginSize.SM).build();
        reply(new ReplyMessage(token, FlexMessage.builder()
                .contents(Bubble.builder().body(mainBox).build())
                .altText("名前登録完了").build()));
    }

    public void tempSuccess(String temp, String name, String token) {
        Text title = createLabel("体温を登録しました", true);
        Text description = createLabel("以下の内容で登録しました", false);
        Map<String, String> data = new HashMap<>();
        data.put("名前", name);
        data.put("体温", temp);
        data.put("日付", new SimpleDateFormat("MM/dd").format(new Date()));
        Box mainBox = Box.builder().layout(FlexLayout.VERTICAL)
                .contents(title, description, listItems(data)).spacing(FlexMarginSize.SM).build();
        Bubble bubble = Bubble.builder().body(mainBox).build();
        FlexMessage message = FlexMessage.builder().contents(bubble).altText("体温登録成功").build();
        reply(new ReplyMessage(token, message));
    }

    private Box listItems(Map<String, String> data) {
        List<FlexComponent> contents = new ArrayList<>();
        for (Map.Entry<String, String> d : data.entrySet()) {
            contents.add(Box.builder().layout(FlexLayout.BASELINE)
                    .spacing(FlexMarginSize.SM)
                    .contents(Text.builder().text(d.getKey()).flex(1).size(FlexFontSize.SM).color("#aaaaaa").build(),
                            Text.builder().text(d.getValue()).flex(4).size(FlexFontSize.SM).color("#666666").build())
                    .build());
        }
        return Box.builder().layout(FlexLayout.VERTICAL)
                .contents(contents).spacing(FlexMarginSize.SM).build();
    }

    public void sendError(ErrorType t, String replyToken) {
        String mes;
        switch (t) {
            case InvalidFormat:
                mes = "不正な形式のメッセージです。テキストメッセージのみ有効です。";
                break;
            case INVALID_TEMP_FORMAT:
                mes = "体温の形式に異常を発見しました。";
                break;
            case SERVER_ERROR:
                mes = "SpreadSheet側でエラーが発生しました。SpreadSheet側で今日の欄が登録されていない可能性があります。";
                break;
            case NAME_NOT_FOUND:
                mes = "入力された名前は見つかりませんでした。SpreadSheet上にある名前と同じ名前を再度入力してください。";
                break;
            case ERROR_UNKNOWN:
            default:
                mes = "不明のエラーが発生しました。";
        }
        logger.info("ERROR, " + t.name() + ", " + mes);
        Text title = createLabel("エラー", true);
        Text content = createLabel(mes, false);
        Box box = Box.builder().contents(title, content).layout(FlexLayout.VERTICAL).build();
        Bubble b = Bubble.builder().body(box).build();
        FlexMessage message = FlexMessage.builder().contents(b).altText("エラーメッセージ").build();
        reply(new ReplyMessage(replyToken, message));
    }

    static Text createLabel(String s, boolean bold) {
        return Text.builder().text(s).weight(bold ? Text.TextWeight.BOLD : Text.TextWeight.REGULAR).wrap(true).build();
    }

    public void broadcastReminder() {
        Box b = Box.builder()
                .layout(FlexLayout.VERTICAL)
                .contents(createLabel("検温忘れずに!", true), createLabel("検温しましょう!このチャットに体温を送信してください!(例:36.4)", false)).build();
        Bubble bubble = Bubble.builder().body(b).build();
        FlexMessage message = FlexMessage.builder()
                .contents(bubble)
                .altText("検温リマインダー")
                .build();
        broadcast(message);
    }

    public void sendLateReminder(String userID) {
        Box b = Box.builder()
                .layout(FlexLayout.VERTICAL)
                .spacing(FlexMarginSize.SM)
                .contents(createLabel("入力を忘れていませんか?", true),
                        createLabel("検温入力が御済みでないようです!忘れずに入力しましょう!", false))
                .build();
        Bubble bubble = Bubble.builder()
                .body(b)
                .build();
        FlexMessage message = FlexMessage.builder()
                .contents(bubble)
                .altText("検温入力忘れていませんか?").build();
        push(new PushMessage(userID, message));
    }
}
