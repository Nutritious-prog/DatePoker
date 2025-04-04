import { Text, View } from "react-native";
import {Link} from "expo-router";

export default function Index() {
    const propertyId = "123"; // Example ID

    return (
    <View
      style={{
        flex: 1,
        justifyContent: "center",
        alignItems: "center",
      }}
    >
        <Text className="font-bold text-3xl my-5 font-rubik"> Welcome to ReState</Text>

        <Link href="/sign-in">Sign In</Link>
        <Link href="/explore">Explore</Link>
        <Link href="/profile">Profile</Link>
        <Link href={{ pathname: "/properties/[id]", params: { id: propertyId } }}>
            Property
        </Link>

    </View>
  );
}
