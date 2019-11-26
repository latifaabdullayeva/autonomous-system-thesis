package com.autonomoussystemserver.server.controller;

import com.autonomoussystemserver.server.controller.model.DistanceDto;
import com.autonomoussystemserver.server.database.model.Devices;
import com.autonomoussystemserver.server.database.model.Distances;
import com.autonomoussystemserver.server.database.model.Personality;
import com.autonomoussystemserver.server.database.repository.DevicesRepository;
import com.autonomoussystemserver.server.database.repository.DistancesRepository;
import com.autonomoussystemserver.server.database.repository.PersonalityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// GET --> POST
@RestController
public class DistancesController {

    @Autowired
    private DistancesRepository distancesRepository;

    @Autowired
    private DevicesRepository devicesRepository;

    @Autowired
    private PersonalityRepository personalityRepository;

//    @Autowired
//    private InteractionTimesRepository interactionTimesRepository;

    // we pass command line arguments to spring server (in order to set up philips hue bridge
    @Value("${hueIPAddress}")
    private String hueIPAddress;

    @Value("${hueUsername}")
    private String hueUsername;

    //    private long initialMillisec = System.currentTimeMillis();
//    private int counter = 1;
//    private MediaPlayerFactory factory;
//    private MediaPlayer audioPlayer;
//    private Semaphore sync = new Semaphore(0);
    private Process process;
    private ProcessBuilder processBuilder = new ProcessBuilder();
    private List<String> processBuilderList;

    private int trackNumber = 0;

    @GetMapping("/distances")
    public org.springframework.data.domain.Page<Distances> getDistances(Pageable pageable) {
//        System.out.println("DistancesController: getDistances()");
        return distancesRepository.findAll(pageable);
    }

    @PostMapping("/distances")
    public Distances postDistance(@RequestBody DistanceDto distanceDto) {
//        System.out.println("DistancesController: postDistance()");

        Distances distances = synchronizedPost(distanceDto);

//        // this method checks if the time is 3 minutes, play a music
//        checkTheTime();

        // we need to find Philips Hue in our network, we get ipAddress and username of hue Lamp from command line
        // TODO describe from where we get username, we get Ip address from the website https://discovery.meethue.com/
        HueRepository hueRepository = new HueRepository(hueIPAddress, hueUsername);

        Devices devNameFrom = devicesRepository.findById(distanceDto.getFromDevice()).orElse(null);
        Devices devNameTo = devicesRepository.findById(distanceDto.getToDevice()).orElse(null);

        String personalityNameofDev = null;
        if (devNameFrom != null) {
            personalityNameofDev = devNameFrom.getDevicePersonality().getPersonality_name();
        }
        Personality personality = personalityRepository.findByPersonalityName(personalityNameofDev);

        if (devNameTo != null && devNameTo.getDeviceType().equals("Lamp")) {
            // TODO: if (devicetype is Lamp - Mascot)
//            if (distances.getDistance() >= 121 && distances.getDistance() <= 370) {
            if (distances.getDistance() <= 45) {
                int brightness = personality.getBri();
                int hue = personality.getHue();
                int saturation = personality.getSat();
                hueRepository.updateBrightness(true, brightness, hue, saturation);

            } else {
                // else if the mascot is outside of range, then turn off the lamp, or let other mascot to change its state
                hueRepository.updateBrightness(false, 0, 0, 0);
            }
        } else if (devNameTo != null && devNameTo.getDeviceType().equals("Speakers")) {
            // TODO: if (devicetype is Lamp - Mascot)
            String musicGenre = personality.getMusic_genre();
            if ((distances.getDistance() <= 45)) {
                playMusic(musicGenre, distanceDto.getFromDevice(), distanceDto.getToDevice(), distances);
            } else {
                stopMusic();
            }
        }
        return distances;
    }

    synchronized private Distances synchronizedPost(DistanceDto distanceDto) {
        // if the distances between two objects exists, delete this row, and then post a new distance value
        // if the values of FROM or TO (i.e the objects are do not exists in database), do not do POST request
        distancesRepository.delete(distanceDto.getFromDevice(), distanceDto.getToDevice());
        distancesRepository.delete(distanceDto.getToDevice(), distanceDto.getFromDevice());

        Devices fromDevice = new Devices();
        Devices toDevice = new Devices();

        fromDevice.setDeviceId(distanceDto.getFromDevice());
        toDevice.setDeviceId(distanceDto.getToDevice());

        Distances distances = new Distances();
        distances.setFromDevice(fromDevice);
        distances.setToDevice(toDevice);
        distances.setDistance(distanceDto.getDistance());

//        System.out.println("DistancesController: distances = " + distances);

        distancesRepository.save(distances);
        return distances;
    }

    List<Process> processList = new ArrayList<Process>();

    private void playMusic(String trackName, Integer from, Integer to, Distances distances) {
        if (trackNumber <= 2) {
            trackNumber = trackNumber + 1;
        } else {
            trackNumber = 1;
        }
        try {
            stopMusic();
//            if (process == null) {
            System.out.println("0 = " + processList);
            if (processList.isEmpty()) {
                process = processBuilder
                        .command("afplay", "/Users/latifaabdullayeva/Desktop/Thesis/ThesisScript/AutonomousSystemThesis/server/src/main/resources/assets/" + trackName + ".mp3")
                        .start();
                System.out.println(" -------------------------------------- Process STARTED!!!! ____ " + process + "; ____" + trackName + ".mp3");
                process.waitFor();
                processList.add(process);
            } else {
                for (Process pr : processList) {
                    pr.destroy();
                    System.out.println("-------------------------------------- Process DESTROYED!!!! ____ " + pr + "; LIST = " + processList);
                }
                processList.clear();
                process = processBuilder
                        .command("afplay", "/Users/latifaabdullayeva/Desktop/Thesis/ThesisScript/AutonomousSystemThesis/server/src/main/resources/assets/" + trackName + ".mp3")
                        .start();
            }
//            for (Process pr : processList) {
//                System.out.println("!!!!!!! " + pr);
//                pr = processBuilder
//                        .command("afplay", "/Users/latifaabdullayeva/Desktop/Thesis/ThesisScript/AutonomousSystemThesis/server/src/main/resources/assets/" + trackName + ".mp3")
//                        .start();
//                System.out.println(" -------------------------------------- Process STARTED!!!! ____ " + pr + "; ____" + trackName + ".mp3");
//
//                processList.add(pr);
//                System.out.println("LIST .... " + processList);
//
//                pr.waitFor();
//                System.out.println("Waits FOR ... ____ " + pr);
//            }
//            processList.clear();

//            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    private void stopMusic() {
        if (process != null && process.isAlive()) {
            process.destroy();

            System.out.println("-------------------------------------- Process DESTROYED!!!! ____ " + process);
        }
    }

//    public synchronized void syncIncrement() {
//        synchronizeCounter++;
//    }
//
//    public synchronized void syncDecrement() {
//        synchronizeCounter--;
//    }
//
//    public synchronized int syncValue() {
//        return synchronizeCounter;
//    }
}

// TODO: We find by IpAddress, but from where we get this IpAddress? get IP from  https://discovery.meethue.com
//        Hue hueData = hueRepository.findByIpAddress("192.168.0.100");
//        hueData.setIpAddress(hueData.getIpAddress());
//        hueData.setUserName(hueData.getUserName());
//        HueRepository hueRepository = new HueRepository(hueData.getIpAddress(), hueData.getUserName());
//         HueRepository hueRepository = new HueRepository("192.168.0.100", "vY5t4oArH-K0BUA7430cb1rJ8mC1DYMzkmBWRr91");
/*
12235	"b0702880-a295-a8ab-f734-031a98a512de"	"Lamp"	[null]
13956	"c08b6bb5-40b7-d552-1db6-a8822ec11ed9"	"Sq"	13886
12958	"88cf77ce-bc91-241a-b8eb-4d041f74acdf"	"Pixel One"	13887

17403	16	13956	12958
17404	20	13956	12235
13955	129	12958	12235

13888	254	57805	"pink"	"rock"	"Extroversion"	198	"green"	4
13889	254	47110	"blue"	"rock"	"Agreeableness"	253	"green"	1
13890	254	12828	"yellow"	"rock"	"Neuroticism"	52	"green"	3
13886	254	3488	"orange"	"rock"	"Openness"	220	"green"	6
13887	254	49460	"violet"	"rock"	"Conscientiousness"	150	"green"	2
 */


//    // this method periodically checks if the 1 minute (60 000 milisec) passed, if yes, it calls the playMusic method
//    private void checkTheTime() {
//        // the number of interaction of each Mascot will be checked only after 3 minutes
//        // the initial time is the time when server get the first "post distance" request,
//        long currentMillisec = System.currentTimeMillis();
//        // here you specify the time, every 55000-600000 milliseconds (55 sec - 60 sec), the music will play
//        // TODO: do not forget to make it 3 minutes (= 180 000 milliseconds)
//        if ((currentMillisec - initialMillisec) >= 55000 * counter && (currentMillisec - initialMillisec) <= 60000 * counter) {
//            counter += 1;
//            chooseWinnerMascot();
//        }
//    }

//    // this method gets from DB the most Active mascot, finds its personality from other table and calls  playMusic method
//    private void chooseWinnerMascot() {
//        // when there are several mascots with the same maximum interactionTimes value, then we just choose the first row (first Mascot)
//        List<Devices> interactionTimes = interactionTimesRepository.findMaximumInteractedMascot(PageRequest.of(0, 1));
//        // winner is the most active Mascot that the DB returns
//        Integer winnerMascot = interactionTimes.get(0).getDeviceId();
//
//        // here we get the personality of the mascot that has ID winner
//        Devices mostActive = Objects.requireNonNull(devicesRepository.findById(winnerMascot).orElse(null));
//        String personalityNameOfMascot = mostActive.getDevicePersonality().getPersonality_name();
//        Personality personalityOfActiveMascot = personalityRepository.findByPersonalityName(personalityNameOfMascot);
//
//        // we get the musicGenre of personality of the winnerMascot
//        String musicGenre = personalityOfActiveMascot.getMusic_genre();
//
//        playMusic(musicGenre + ".mp3");
//    }
