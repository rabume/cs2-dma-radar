<template>
    <div class="control">
        <button @click="rotateMap">Rotate Map</button>
        <br /><br />
        <div>Tick: {{ gameInfo.tick }}</div>
        <div>Average tick: {{ parseInt(allTickVal / tickTimes) }}</div>

        <div v-for="(showTeammate, index) in showTeammates" :key="index" @click="toggleTeammate(index)">
            Show {{ teammateNames[index] }}<input type="checkbox" v-model="showTeammates[index]" />
        </div>

        <div @click="showEnemiesChanges">Show Enemies<input type="checkbox" v-model="showEnemies" /></div>
    </div>
    <div id="map-container">
        <div id="map" :style="{ transform: `rotate(${rotationAngle}deg)` }"></div>
    </div>
</template>

<script>
import { Client, Versions } from '@stomp/stompjs'

import enemyIcon from '/src/assets/icons/enemy_icon.png'
import enemyIconHvd from '/src/assets/icons/enemy_icon_hvd.png'
import localPlayerIcon from '/src/assets/icons/localPlayer_icon.png'
import defaultTeammateIcon from '/src/assets/icons/teammate_icon.png'
import teammateIcon0 from '/src/assets/icons/teammate_icon_0.png'
import teammateIcon1 from '/src/assets/icons/teammate_icon_1.png'
import teammateIcon2 from '/src/assets/icons/teammate_icon_2.png'
import teammateIcon3 from '/src/assets/icons/teammate_icon_3.png'
import teammateIcon4 from '/src/assets/icons/teammate_icon_4.png'

import de_ancient_radar from '/src/assets/map/de_ancient_radar.png'
import de_dust2_radar from '/src/assets/map/de_dust2_radar.png'
import de_inferno_radar from '/src/assets/map/de_inferno_radar.png'
import de_mirage_radar from '/src/assets/map/de_mirage_radar.png'
import de_train_radar from '/src/assets/map/de_train_radar.png'
import de_nuke_radar from '/src/assets/map/de_nuke_radar.png'
import de_nuke_lower_radar from '/src/assets/map/de_nuke_lower_radar.png'
import de_overpass_radar from '/src/assets/map/de_overpass_radar.png'
import de_vertigo_radar from '/src/assets/map/de_vertigo_radar.png'
import de_vertigo_lower_radar from '/src/assets/map/de_vertigo_lower_radar.png'

const teammateIcons = {
    0: teammateIcon0,
    1: teammateIcon1,
    2: teammateIcon2,
    3: teammateIcon3,
    4: teammateIcon4
}

const mapRadar = {
    de_ancient: {
        map: de_ancient_radar,
        bounds: [
            [-294, -289],
            [217, 213]
        ]
    },
    de_dust2: {
        map: de_dust2_radar,
        bounds: [
            [-127, -247],
            [323, 202]
        ]
    },
    de_inferno: {
        map: de_inferno_radar,
        bounds: [
            [-112, -206],
            [380, 292]
        ]
    },
    de_mirage: {
        map: de_mirage_radar,
        bounds: [
            [-340, -322],
            [172, 188]
        ]
    },
    de_nuke: {
        needChangeMap: true,
        map: de_nuke_radar,
        mapLower: de_nuke_lower_radar,
        lowerValue: -480,
        bounds: [
            [-441, -329],
            [304, 357]
        ]
    },
    de_overpass: {
        map: de_overpass_radar,
        bounds: [
            [-361, -479],
            [181, 42]
        ]
    },
    de_vertigo: {
        needChangeMap: true,
        map: de_vertigo_radar,
        mapLower: de_vertigo_lower_radar,
        lowerValue: 11720,
        bounds: [
            [-223, -312],
            [172, 84]
        ]
    },
    de_train: {
        map: de_train_radar,
        bounds: [
            [-560, -482],
            [-45, 50]
        ]
    }
}

export default {
    data() {
        return {
            allTickVal: 0,
            tickTimes: 0,
            showTeammates: [true, true, true, true, true],
            showEnemies: true,
            teammateNames: ['Blue', 'Green', 'Yellow', 'Orange', 'Purple'],
            zoom: 1,
            lastMapName: null,
            gameInfo: {},
            MarkerList: [],
            map: null,
            imageOverLay: null,
            bounds: [
                [-330, -315],
                [155, 185]
            ],
            XSize: 500,
            YSize: 500,
            rotationAngle: 0
        }
    },
    created() {
        const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
        const wsPort = window.location.port ? `:${window.location.port}` : ''
        const wsUrl = `${wsProtocol}://${window.location.hostname}${wsPort}/radar`

        const client = new Client({
            heartbeatIncoming: 0,
            heartbeatOutgoing: 0,
            stompVersions: new Versions([Versions.V1_2]),
            brokerURL: wsUrl,
            onConnect: () => {
                client.subscribe('/topic/radar', (radar) => {
                    let msg = JSON.parse(radar.body)
                    this.gameInfo.mapName = msg.mapName
                    this.gameInfo.tick = msg.tick
                    this.tickTimes++
                    this.allTickVal += msg.tick
                    this.initPlayerList(msg.playerList)
                })
            },
            onWebSocketError: (error) => {
                console.error('Error with websocket', error)
            },
            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message'])
                console.error('Additional details: ' + frame.body)
            }
        })

        client.activate()
    },
    mounted() {
        window.addEventListener('keydown', this.KeyDown, true)
        this.initMap()
    },
    methods: {
        toggleTeammate(index) {
            this.showTeammates[index] = !this.showTeammates[index]
        },
        showTeammatesChange() {
            this.showTeammates = !this.showTeammates
        },
        showEnemiesChanges() {
            this.showEnemies = !this.showEnemies
        },
        initPlayerList(data) {
            let knowMap = !!mapRadar[this.gameInfo.mapName]
            if (knowMap) {
                this.initKnowMap()
            } else {
                this.initUnKnowMap()
            }
            if (data.length > 0) {
                this.updatePlayerMarker(data, knowMap)
            }
        },
        initKnowMap() {
            if (this.lastMapName !== this.gameInfo.mapName) {
                this.lastMapName = this.gameInfo.mapName
                if (this.imageOverLay != null) {
                    this.allTickVal = 0
                    this.tickTimes = 0
                    this.map.removeLayer(this.imageOverLay)
                }
                this.imageOverLay = L.imageOverlay(mapRadar[this.gameInfo.mapName].map, mapRadar[this.gameInfo.mapName].bounds, {
                    interactive: true,
                    opacity: 1
                }).addTo(this.map)
                this.map.setView(this.imageOverLay.getBounds().getCenter())
            }
        },
        initUnKnowMap() {
            if (this.imageOverLay != null) {
                this.allTickVal = 0
                this.tickTimes = 0
                this.map.removeLayer(this.imageOverLay)
                this.imageOverLay = null
            }
        },
        updatePlayerMarker(data, knowMap) {
            let mlist = []
            data.forEach((item) => {
                if (item.alive) {
                    if (
                        (item.enemy && !this.showEnemies) ||
                        (!item.enemy && !this.showTeammates[item.compTeammateColor] && item.compTeammateColor !== -1)
                    ) {
                        return
                    }

                    let point = L.latLng(item.x / 10, item.y / 10)
                    let iconUrl

                    if (item.compTeammateColor === -1) {
                        if (item.localPlayer) {
                            iconUrl = localPlayerIcon
                        } else if (item.enemy) {
                            iconUrl = item.sameLevel ? enemyIcon : enemyIconHvd
                        } else {
                            iconUrl = defaultTeammateIcon
                        }
                    } else {
                        if (item.localPlayer) {
                            let teammateColorIconIndex = item.compTeammateColor
                            iconUrl = teammateIcons[teammateColorIconIndex] || localPlayerIcon
                        } else if (item.enemy) {
                            iconUrl = item.sameLevel ? enemyIcon : enemyIconHvd
                        } else {
                            let teammateColorIconIndex = item.compTeammateColor
                            iconUrl = teammateIcons[teammateColorIconIndex] || defaultTeammateIcon
                        }
                    }

                    let icon = L.icon({
                        iconUrl: iconUrl,
                        iconSize: [40, 40],
                        iconAnchor: [20, 26.5]
                    })

                    let marker = this.addMarker(point, icon, item.localPlayer ? (knowMap ? item.angles : 0) : item.angles)

                    // Add Health
                    let healthIcon = L.divIcon({
                        className: 'health-marker',
                        html: `<div class="health-text">${item.health}</div>`,
                        iconSize: [40, 40],
                        iconAnchor: [20, 8]
                    })

                    let healthMarker = L.marker(point, { icon: healthIcon })

                    mlist.push(marker)
                    mlist.push(healthMarker)
                }

                if (item.localPlayer && knowMap) {
                    if (mapRadar[this.gameInfo.mapName].needChangeMap) {
                        if (item.z > mapRadar[this.gameInfo.mapName].lowerValue) {
                            this.map.removeLayer(this.imageOverLay)
                            this.imageOverLay = L.imageOverlay(mapRadar[this.gameInfo.mapName].map, mapRadar[this.gameInfo.mapName].bounds, {
                                interactive: true,
                                opacity: 1
                            }).addTo(this.map)
                        } else {
                            this.map.removeLayer(this.imageOverLay)
                            this.imageOverLay = L.imageOverlay(
                                mapRadar[this.gameInfo.mapName].mapLower,
                                mapRadar[this.gameInfo.mapName].bounds
                            ).addTo(this.map)
                        }
                    }
                }
            })

            if (this.layerGroup != null) {
                this.map.removeLayer(this.layerGroup)
            }
            this.MarkerList = mlist
            this.layerGroup = L.layerGroup(this.MarkerList)
            this.map.addLayer(this.layerGroup)
        },
        reloadMap() {
            if (this.imageOverLay != null) {
                this.map.removeLayer(this.imageOverLay)
            }
            this.bounds[1] = [this.bounds[0][0] + this.XSize, this.bounds[0][1] + this.YSize]
            this.imageOverLay = L.imageOverlay(mapRadar[this.gameInfo.mapName].map, this.bounds).addTo(this.map)
        },
        KeyDown(e) {
            switch (e.keyCode) {
                case 96:
                    this.XSize = 500
                    this.YSize = 500
                    this.reloadMap()
                    break
                case 98:
                    this.XSize += 1
                    this.reloadMap()
                    break
                case 97:
                    this.XSize -= 1
                    this.reloadMap()
                    break
                case 101:
                    this.YSize += 1
                    this.reloadMap()
                    break
                case 100:
                    this.YSize -= 1
                    this.reloadMap()
                    break
                case 37:
                    this.bounds[0][1] += 1
                    this.reloadMap()
                    break
                case 38:
                    this.bounds[0][0] -= 1
                    this.reloadMap()
                    break
                case 39:
                    this.bounds[0][1] -= 1
                    this.reloadMap()
                    break
                case 40:
                    this.bounds[0][0] += 1
                    this.reloadMap()
                    break
            }
        },
        initMap() {
            this.map = L.map('map', {
                center: [0, 0],
                zoom: this.zoom,
                crs: L.CRS.Simple,
                maxZoom: 3,
                minZoom: 0
            })
        },
        addMarker(point, icon, angles) {
            return L.marker(point, {
                icon: icon,
                rotationAngle: angles
            })
        },
        rotateMap() {
            this.rotationAngle = (this.rotationAngle + 90) % 360
            const mapContainer = document.getElementById('map')
            mapContainer.style.transform = `rotate(${this.rotationAngle}deg)`
            this.updatePlayerMarker(this.gameInfo.playerList, !!mapRadar[this.gameInfo.mapName])
        }
    }
}
</script>

<style scoped>
#map-container {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 0;
}

#map {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 0;
    transition: transform 0.5s ease-in-out;
}

.control {
    position: absolute;
    top: 100px;
    left: 10px;
    z-index: 1;
    background-color: aliceblue;
    border-radius: 10px;
    padding: 5px 10px;
}

.health-text {
    text-align: center;
}

.control div {
    margin-bottom: 5px;
}
</style>
