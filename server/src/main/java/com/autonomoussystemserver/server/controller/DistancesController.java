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

// GET --> POST
@RestController
public class DistancesController {

    @Autowired
    private DistancesRepository distancesRepository;

    @Autowired
    private DevicesRepository devicesRepository;

    @Autowired
    private PersonalityRepository personalityRepository;

    // we pass command line arguments to spring server (in order to set up philips hue bridge
    @Value("${ipAddress}")
    private String ipAddress;

    @Value("${username}")
    private String username;

    @GetMapping("/distances")
    public org.springframework.data.domain.Page<Distances> getDistances(Pageable pageable) {
        System.out.println("------------------------------------------------------------");
        System.out.println("DistanceController -> GET getDistances");
        return distancesRepository.findAll(pageable);
    }

    @PostMapping("/distances")
    public Distances postDistance(@RequestBody DistanceDto distanceDto) {
        System.out.println("------------------------------------------------------------");
        System.out.println("DistanceController -> POST postDistance");
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

        // Proxemics Theory

        distancesRepository.save(distances);
        System.out.println("DistanceController -> POST distances: " + distances);
        System.out.println("Hue distances.getDistance(): " + distances.getDistance());

        // TODO: We find by IpAddress, but from where we get this IpAddress? get IP from  https://discovery.meethue.com
//        Hue hueData = hueRepository.findByIpAddress("192.168.0.100");
//        hueData.setIpAddress(hueData.getIpAddress());
//        hueData.setUserName(hueData.getUserName());
//        System.out.println("Backend: " + "Hue hueData.getIpAddress(): " + hueData.getIpAddress() + "; hueData.getUserName(): " + hueData.getUserName());
//        HueRepository hueRepository = new HueRepository(hueData.getIpAddress(), hueData.getUserName());
//         HueRepository hueRepository = new HueRepository("192.168.0.100", "vY5t4oArH-K0BUA7430cb1rJ8mC1DYMzkmBWRr91");

        // we need to find Philips Hue in our network
        // we get ipAddress and username of hue Lamp from comand line
        // TODO describe from where we get username
        // we get Ip address from the website https://discovery.meethue.com/
        HueRepository hueRepository = new HueRepository(ipAddress, username);
        System.out.println("DistanceController hueRepository, [" + hueRepository + "]; [" + ipAddress + "]; [" + username + "]");

        Devices devNameFrom = devicesRepository.findById(distanceDto.getFromDevice()).orElse(null);
        Devices devNameTo = devicesRepository.findById(distanceDto.getToDevice()).orElse(null);

        System.out.println("devNameFrom.getDeviceType() = " + devNameFrom.getDeviceType());
        System.out.println("devNameTo.getDeviceType() = " + devNameTo.getDeviceType());
        if (devNameTo.getDeviceType().equals("Lamp")) {
            if (distances.getDistance() >= 120 && distances.getDistance() <= 370) { //
                String personalityNameofDev = devNameFrom.getDevicePersonality().getPersonality_name();
                Personality personality = personalityRepository.findByPersonalityName(personalityNameofDev);
                System.out.println("DistanceController Personality personality = " + personality + "; personalityNameofDev = " + personalityNameofDev);
                int brightness = personality.getBri();
                int hue = personality.getHue();
                int saturation = personality.getSat();
                hueRepository.updateBrightness(brightness, hue, saturation);
                System.out.println("Hue hueRepository.updateBrightness() brightness = [" + brightness + "]; hue = [" + hue + "]; saturation = [" + saturation + "]");
            }
        }
        return distances;
    }
}

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