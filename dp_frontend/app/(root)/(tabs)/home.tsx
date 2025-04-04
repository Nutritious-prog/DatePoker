import React from "react";
import { View, Text, Image, TouchableOpacity, ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import {Link} from "expo-router";
import images from "@/constants/images";
import icons from "@/constants/icons";

const HomeScreen = () => {
    return (
        <SafeAreaView className="bg-background h-full">
            <ScrollView contentContainerStyle={{ paddingBottom: 40 }}>
                {/* Header */}
                <View className="flex flex-row items-center justify-between px-5 mt-5">
                    <View className="flex flex-row">
                    <Image
                        source={images.avatar}
                        className="size-10 relative rounded-full"
                    />
                    <View className="ml-3">
                        <Text className="text-xs text-black-200">Good Morning</Text>
                        <Text className="text-lg font-rubik-bold text-black-300">Adrian Hajdin</Text>
                    </View>
                    </View>
                    <TouchableOpacity className="w-10 h-10 rounded-full bg-white items-center justify-center shadow">
                        <Image
                            source={icons.bell}
                            className="size-6 relative rounded-full"
                        />
                    </TouchableOpacity>
                </View>

                {/* Main Title */}
                <View className="items-center mt-20">
                    <Text className="text-4xl font-rubik-semibold text-black-300">Let’s Plan</Text>
                    <Text className="text-5xl font-rubik-bold text-primary-300 mt-1">Your Ideal Date</Text>
                    <Text className="text-md text-black-200 mt-2">Secretly pick your favorites and see what matches</Text>
                </View>

                {/* Illustration */}
                <View className="mx-5 mt-6 overflow-hidden">
                    <Image
                        source={images.homeCouple}
                        className="w-full h-64"
                        resizeMode="contain"
                    />
                </View>

                {/* Buttons */}
                <View className="px-5 mt-8  flex flex-col items-center justify-between h-[18%]">
                    <Link href="/home">
                        <TouchableOpacity className="bg-primary-300 py-4 w-full rounded-full items-center shadow">
                            <Text className="text-white font-rubik-bold text-base">START PLANNING</Text>
                        </TouchableOpacity>
                    </Link>

                    <Link href="/home">
                        <TouchableOpacity className="bg-primary-200 py-4 w-full rounded-full items-center shadow" style={{ backgroundColor: '#900B09' }}>
                            <Text className="text-white font-rubik-bold text-base">JOIN YOUR DATE’S ROOM</Text>
                        </TouchableOpacity>
                    </Link>
                </View>

                {/* Info Links */}
                <View className="px-5 mt-6 space-y-1 flex items-center justify-center">
                    <TouchableOpacity>
                        <Text className="text-black-100 text-sm">• How does this work?</Text>
                    </TouchableOpacity>
                    <TouchableOpacity>
                        <Text className="text-black-100 text-sm">• Want to play solo?</Text>
                    </TouchableOpacity>
                </View>

                {/* Bottom Navigation Placeholder (optional) */}
                {/* This would normally be handled by a tab navigator */}
            </ScrollView>
        </SafeAreaView>
    );
};

export default HomeScreen;