package com.diarization;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import com.google.cloud.speech.v1p1beta1.SpeakerDiarizationConfig;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception {
        System.out.println( "Hello World!" );
//      transcribeDiarizationGcs("/home/aditya/project/speaker_diarization/testAudio1.mp3");
      transcribeDiarizationGcs("gs://test_diarization/Echo videos/testAudio1.mp3");
    }

/**
 * Transcribe the given audio file using speaker diarization.
 *
 * @param fileName the path to an audio file.
 */
public static void transcribeDiarization(String fileName) throws Exception {
  Path path = Paths.get(fileName);
  byte[] content = Files.readAllBytes(path);
  BufferedWriter writer = new BufferedWriter(new FileWriter("/home/aditya/project/speaker_diarization/sample.txt"));


  try (SpeechClient speechClient = SpeechClient.create()) {
    // Get the contents of the local audio file
    RecognitionAudio recognitionAudio =
        RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();

    SpeakerDiarizationConfig speakerDiarizationConfig = SpeakerDiarizationConfig.newBuilder()
            .setEnableSpeakerDiarization(true)
            .setMinSpeakerCount(2)
            .setMaxSpeakerCount(10)
            .build();

    // Configure request to enable Speaker diarization
    RecognitionConfig config = RecognitionConfig.newBuilder()
            .setEncoding(RecognitionConfig.AudioEncoding.MP3)
            .setLanguageCode("en-US")
            .setSampleRateHertz(8000)
            .setDiarizationConfig(speakerDiarizationConfig)
            .build();

    OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
            speechClient.longRunningRecognizeAsync(config, recognitionAudio);

    while (!response.isDone()) {
      System.out.println("Waiting for response...");
      Thread.sleep(10000);
    }

    // Speaker Tags are only included in the last result object, which has only one alternative.
    LongRunningRecognizeResponse longRunningRecognizeResponse = response.get();
    SpeechRecognitionAlternative alternative =
            longRunningRecognizeResponse.getResults(
                    longRunningRecognizeResponse.getResultsCount() - 1)
                    .getAlternatives(0);

    // The alternative is made up of WordInfo objects that contain the speaker_tag.
    WordInfo wordInfo = alternative.getWords(0);
    int currentSpeakerTag = wordInfo.getSpeakerTag();

    // For each word, get all the words associated with one speaker, once the speaker changes,
    // add a new line with the new speaker and their spoken words.
    StringBuilder speakerWords = new StringBuilder(
            String.format("Speaker %d: %s", wordInfo.getSpeakerTag(), wordInfo.getWord()));

    for (int i = 1; i < alternative.getWordsCount(); i++) {
      wordInfo = alternative.getWords(i);
      if (currentSpeakerTag == wordInfo.getSpeakerTag()) {
        speakerWords.append(" ");
        speakerWords.append(wordInfo.getWord());
      } else {
        speakerWords.append(
                String.format("\nSpeaker %d: %s",
                        wordInfo.getSpeakerTag(),
                        wordInfo.getWord()));
        currentSpeakerTag = wordInfo.getSpeakerTag();
      }
    }

    System.out.println(speakerWords.toString());
    // Perform the transcription request
//    RecognizeResponse recognizeResponse = speechClient.recognize(config, recognitionAudio);


    // Speaker Tags are only included in the last result object, which has only one alternative.
//    SpeechRecognitionAlternative alternative =
//            recognizeResponse.getResults(
//                    recognizeResponse.getResultsCount() - 1).getAlternatives(0);

//    // The alternative is made up of WordInfo objects that contain the speaker_tag.
//    WordInfo wordInfo = alternative.getWords(0);
//    int currentSpeakerTag = wordInfo.getSpeakerTag();
//
//    // For each word, get all the words associated with one speaker, once the speaker changes,
//    // add a new line with the new speaker and their spoken words.
//    StringBuilder speakerWords = new StringBuilder(
//            String.format("Speaker %d: %s", wordInfo.getSpeakerTag(), wordInfo.getWord()));
//
//    for (int i = 1; i < alternative.getWordsCount(); i++) {
//      wordInfo = alternative.getWords(i);
//      if (currentSpeakerTag == wordInfo.getSpeakerTag()) {
//        speakerWords.append(" ");
//        speakerWords.append(wordInfo.getWord());
//      } else {
//        speakerWords.append(
//                String.format("\nSpeaker %d: %s",
//                        wordInfo.getSpeakerTag(),
//                        wordInfo.getWord()));
//        currentSpeakerTag = wordInfo.getSpeakerTag();
//      }
//    }

    writer.append(speakerWords.toString());
      }
  writer.close();
}


  public static void transcribeDiarizationGcs(String gcsUri) throws Exception {

    BufferedWriter writer = new BufferedWriter(new FileWriter("/home/aditya/project/speaker_diarization_api/test_audio_1_final.txt"));

//    Path path = Paths.get(gcsUri);
//    byte[] content = Files.readAllBytes(path);

    try (SpeechClient speechClient = SpeechClient.create()) {
      SpeakerDiarizationConfig speakerDiarizationConfig = SpeakerDiarizationConfig.newBuilder()
              .setEnableSpeakerDiarization(true)
              .setMinSpeakerCount(15)
              .setMaxSpeakerCount(20)
              .build();

        RecognitionMetadata recognitionMetadata =
                RecognitionMetadata.newBuilder()
                .setOriginalMediaType(RecognitionMetadata.OriginalMediaType.AUDIO)
                .setRecordingDeviceType(RecognitionMetadata.RecordingDeviceType.SMARTPHONE)
                .setOriginalMimeType("audio/aac")
                .build();

      // Configure request to enable Speaker diarization
      RecognitionConfig config =
              RecognitionConfig.newBuilder()
                      .setEncoding(RecognitionConfig.AudioEncoding.MP3)
                      .setLanguageCode("en-IN")
                      .setSampleRateHertz(48000)
                      .mergeMetadata(recognitionMetadata)
                      .setDiarizationConfig(speakerDiarizationConfig)
                      .build();

      // Set the remote path for the audio file
      RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

//      RecognitionAudio audio =
//              RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();


      // Use non-blocking call for getting file transcription
      OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
              speechClient.longRunningRecognizeAsync(config, audio);

      while (!response.isDone()) {
        System.out.println("Waiting for response...");
        Thread.sleep(10000);
      }

      System.out.println("Response : "+response);
      System.out.println("Response toString : "+response.toString());
      System.out.println("GetName : "+response.getName());
      // Speaker Tags are only included in the last result object, which has only one alternative.
      LongRunningRecognizeResponse longRunningRecognizeResponse = response.get();
      System.out.println("longRunningRecognizeResponse : "+longRunningRecognizeResponse);
      System.out.println("longRunningRecognizeResponse toString : "+longRunningRecognizeResponse.toString());

//      SpeechRecognitionAlternative alternative =
//              longRunningRecognizeResponse.getResults(
//                      longRunningRecognizeResponse.getResultsCount() - 1)
//                      .getAlternatives(0);

System.out.println("longRunningRecognizeResponse.getResultsCount() : "+longRunningRecognizeResponse.getResultsCount());

      SpeechRecognitionResult test =
              longRunningRecognizeResponse.getResults(
                      longRunningRecognizeResponse.getResultsCount() - 1);
      System.out.println("Test : "+test);
      writer.append(test.toString());

              for(int i=0 ;i<test.getAlternativesCount();i++)
              {
                SpeechRecognitionAlternative alternative = test.getAlternatives(i);


                // The alternative is made up of WordInfo objects that contain the speaker_tag.
                WordInfo wordInfo = alternative.getWords(0);
                int currentSpeakerTag = wordInfo.getSpeakerTag();

                // For each word, get all the words associated with one speaker, once the speaker changes,
                // add a new line with the new speaker and their spoken words.
                StringBuilder speakerWords = new StringBuilder(
                        String.format("Speaker %d: %s", wordInfo.getSpeakerTag(), wordInfo.getWord()));

                for (int ii = 1; ii < alternative.getWordsCount(); ii++) {
                  wordInfo = alternative.getWords(ii);
                  if (currentSpeakerTag == wordInfo.getSpeakerTag()) {
                    speakerWords.append(" ");
                    speakerWords.append(wordInfo.getWord());
                  } else {
                    speakerWords.append(
                            String.format("\nSpeaker %d: %s",
                                    wordInfo.getSpeakerTag(),
                                    wordInfo.getWord()));
                    currentSpeakerTag = wordInfo.getSpeakerTag();
                  }
                }

                System.out.println(speakerWords.toString());
                writer.append(speakerWords.toString());

              }
writer.close();

    }
  }
  // [END speech_transcribe_diarization_gcs_beta]

  // [START speech_transcribe_multichannel_beta]
  /**
   * Transcribe a local audio file with multi-channel recognition
   *
   * @param fileName the path to local audio file
   */
  public static void transcribeMultiChannel(String fileName) throws Exception {
    Path path = Paths.get(fileName);
    byte[] content = Files.readAllBytes(path);

    try (SpeechClient speechClient = SpeechClient.create()) {
      // Get the contents of the local audio file
      RecognitionAudio recognitionAudio =
              RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();

      // Configure request to enable multiple channels
      RecognitionConfig config =
              RecognitionConfig.newBuilder()
                      .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                      .setLanguageCode("en-US")
                      .setSampleRateHertz(44100)
                      .setAudioChannelCount(2)
                      .setEnableSeparateRecognitionPerChannel(true)
                      .build();

      // Perform the transcription request
      RecognizeResponse recognizeResponse = speechClient.recognize(config, recognitionAudio);

      // Print out the results
      for (SpeechRecognitionResult result : recognizeResponse.getResultsList()) {
        // There can be several alternative transcripts for a given chunk of speech. Just use the
        // first (most likely) one here.
        SpeechRecognitionAlternative alternative = result.getAlternatives(0);
        System.out.format("Transcript : %s\n", alternative.getTranscript());
        System.out.printf("Channel Tag : %s\n\n", result.getChannelTag());
      }
    }
  }

}
