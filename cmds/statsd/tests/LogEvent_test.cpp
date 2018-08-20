// Copyright (C) 2017 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include <gtest/gtest.h>
#include <log/log_event_list.h>
#include "src/logd/LogEvent.h"

#ifdef __ANDROID__

namespace android {
namespace os {
namespace statsd {

TEST(LogEventTest, TestLogParsing) {
    LogEvent event1(1, 2000);

    std::vector<AttributionNodeInternal> nodes;

    AttributionNodeInternal node1;
    node1.set_uid(1000);
    node1.set_tag("tag1");
    nodes.push_back(node1);

    AttributionNodeInternal node2;
    node2.set_uid(2000);
    node2.set_tag("tag2");
    nodes.push_back(node2);

    event1.write(nodes);
    event1.write("hello");
    event1.write((int32_t)10);
    event1.write((int64_t)20);
    event1.write((float)1.1);
    event1.init();

    const auto& items = event1.getValues();
    EXPECT_EQ((size_t)8, items.size());
    EXPECT_EQ(1, event1.GetTagId());

    const FieldValue& item0 = event1.getValues()[0];
    EXPECT_EQ(0x2010101, item0.mField.getField());
    EXPECT_EQ(Type::INT, item0.mValue.getType());
    EXPECT_EQ(1000, item0.mValue.int_value);

    const FieldValue& item1 = event1.getValues()[1];
    EXPECT_EQ(0x2010182, item1.mField.getField());
    EXPECT_EQ(Type::STRING, item1.mValue.getType());
    EXPECT_EQ("tag1", item1.mValue.str_value);

    const FieldValue& item2 = event1.getValues()[2];
    EXPECT_EQ(0x2018201, item2.mField.getField());
    EXPECT_EQ(Type::INT, item2.mValue.getType());
    EXPECT_EQ(2000, item2.mValue.int_value);

    const FieldValue& item3 = event1.getValues()[3];
    EXPECT_EQ(0x2018282, item3.mField.getField());
    EXPECT_EQ(Type::STRING, item3.mValue.getType());
    EXPECT_EQ("tag2", item3.mValue.str_value);

    const FieldValue& item4 = event1.getValues()[4];
    EXPECT_EQ(0x20000, item4.mField.getField());
    EXPECT_EQ(Type::STRING, item4.mValue.getType());
    EXPECT_EQ("hello", item4.mValue.str_value);

    const FieldValue& item5 = event1.getValues()[5];
    EXPECT_EQ(0x30000, item5.mField.getField());
    EXPECT_EQ(Type::INT, item5.mValue.getType());
    EXPECT_EQ(10, item5.mValue.int_value);

    const FieldValue& item6 = event1.getValues()[6];
    EXPECT_EQ(0x40000, item6.mField.getField());
    EXPECT_EQ(Type::LONG, item6.mValue.getType());
    EXPECT_EQ((int64_t)20, item6.mValue.long_value);

    const FieldValue& item7 = event1.getValues()[7];
    EXPECT_EQ(0x50000, item7.mField.getField());
    EXPECT_EQ(Type::FLOAT, item7.mValue.getType());
    EXPECT_EQ((float)1.1, item7.mValue.float_value);
}

TEST(LogEventTest, TestLogParsing2) {
    LogEvent event1(1, 2000);

    std::vector<AttributionNodeInternal> nodes;

    event1.write("hello");

    // repeated msg can be in the middle
    AttributionNodeInternal node1;
    node1.set_uid(1000);
    node1.set_tag("tag1");
    nodes.push_back(node1);

    AttributionNodeInternal node2;
    node2.set_uid(2000);
    node2.set_tag("tag2");
    nodes.push_back(node2);
    event1.write(nodes);

    event1.write((int32_t)10);
    event1.write((int64_t)20);
    event1.write((float)1.1);
    event1.init();

    const auto& items = event1.getValues();
    EXPECT_EQ((size_t)8, items.size());
    EXPECT_EQ(1, event1.GetTagId());

    const FieldValue& item = event1.getValues()[0];
    EXPECT_EQ(0x00010000, item.mField.getField());
    EXPECT_EQ(Type::STRING, item.mValue.getType());
    EXPECT_EQ("hello", item.mValue.str_value);

    const FieldValue& item0 = event1.getValues()[1];
    EXPECT_EQ(0x2020101, item0.mField.getField());
    EXPECT_EQ(Type::INT, item0.mValue.getType());
    EXPECT_EQ(1000, item0.mValue.int_value);

    const FieldValue& item1 = event1.getValues()[2];
    EXPECT_EQ(0x2020182, item1.mField.getField());
    EXPECT_EQ(Type::STRING, item1.mValue.getType());
    EXPECT_EQ("tag1", item1.mValue.str_value);

    const FieldValue& item2 = event1.getValues()[3];
    EXPECT_EQ(0x2028201, item2.mField.getField());
    EXPECT_EQ(Type::INT, item2.mValue.getType());
    EXPECT_EQ(2000, item2.mValue.int_value);

    const FieldValue& item3 = event1.getValues()[4];
    EXPECT_EQ(0x2028282, item3.mField.getField());
    EXPECT_EQ(Type::STRING, item3.mValue.getType());
    EXPECT_EQ("tag2", item3.mValue.str_value);

    const FieldValue& item5 = event1.getValues()[5];
    EXPECT_EQ(0x30000, item5.mField.getField());
    EXPECT_EQ(Type::INT, item5.mValue.getType());
    EXPECT_EQ(10, item5.mValue.int_value);

    const FieldValue& item6 = event1.getValues()[6];
    EXPECT_EQ(0x40000, item6.mField.getField());
    EXPECT_EQ(Type::LONG, item6.mValue.getType());
    EXPECT_EQ((int64_t)20, item6.mValue.long_value);

    const FieldValue& item7 = event1.getValues()[7];
    EXPECT_EQ(0x50000, item7.mField.getField());
    EXPECT_EQ(Type::FLOAT, item7.mValue.getType());
    EXPECT_EQ((float)1.1, item7.mValue.float_value);
}

TEST(LogEventTest, TestKeyValuePairsAtomParsing) {
    std::map<int32_t, int64_t> int_map;
    std::map<int32_t, std::string> string_map;
    std::map<int32_t, float> float_map;

    int_map[11] = 123L;
    int_map[22] = 345L;

    string_map[1] = "test2";
    string_map[2] = "test1";

    float_map[111] = 2.2f;
    float_map[222] = 1.1f;

    LogEvent event1(83, 2000, 2001, 10001, int_map, string_map, float_map);
    event1.init();

    EXPECT_EQ(83, event1.GetTagId());
    EXPECT_EQ((int64_t)2000, event1.GetLogdTimestampNs());
    EXPECT_EQ((int64_t)2001, event1.GetElapsedTimestampNs());

    const auto& items = event1.getValues();
    EXPECT_EQ((size_t)13, items.size());

    const FieldValue& item0 = event1.getValues()[0];
    EXPECT_EQ(0x00010000, item0.mField.getField());
    EXPECT_EQ(Type::INT, item0.mValue.getType());
    EXPECT_EQ(10001, item0.mValue.int_value);

    const FieldValue& item1 = event1.getValues()[1];
    EXPECT_EQ(0x2020101, item1.mField.getField());
    EXPECT_EQ(Type::INT, item1.mValue.getType());
    EXPECT_EQ(11, item1.mValue.int_value);

    const FieldValue& item2 = event1.getValues()[2];
    EXPECT_EQ(0x2020182, item2.mField.getField());
    EXPECT_EQ(Type::LONG, item2.mValue.getType());
    EXPECT_EQ(123L, item2.mValue.long_value);

    const FieldValue& item3 = event1.getValues()[3];
    EXPECT_EQ(0x2020201, item3.mField.getField());
    EXPECT_EQ(Type::INT, item3.mValue.getType());
    EXPECT_EQ(22, item3.mValue.int_value);

    const FieldValue& item4 = event1.getValues()[4];
    EXPECT_EQ(0x2020282, item4.mField.getField());
    EXPECT_EQ(Type::LONG, item4.mValue.getType());
    EXPECT_EQ(345L, item4.mValue.long_value);

    const FieldValue& item5 = event1.getValues()[5];
    EXPECT_EQ(0x2020301, item5.mField.getField());
    EXPECT_EQ(Type::INT, item5.mValue.getType());
    EXPECT_EQ(1, item5.mValue.int_value);

    const FieldValue& item6 = event1.getValues()[6];
    EXPECT_EQ(0x2020383, item6.mField.getField());
    EXPECT_EQ(Type::STRING, item6.mValue.getType());
    EXPECT_EQ("test2", item6.mValue.str_value);

    const FieldValue& item7 = event1.getValues()[7];
    EXPECT_EQ(0x2020401, item7.mField.getField());
    EXPECT_EQ(Type::INT, item7.mValue.getType());
    EXPECT_EQ(2, item7.mValue.int_value);

    const FieldValue& item8 = event1.getValues()[8];
    EXPECT_EQ(0x2020483, item8.mField.getField());
    EXPECT_EQ(Type::STRING, item8.mValue.getType());
    EXPECT_EQ("test1", item8.mValue.str_value);

    const FieldValue& item9 = event1.getValues()[9];
    EXPECT_EQ(0x2020501, item9.mField.getField());
    EXPECT_EQ(Type::INT, item9.mValue.getType());
    EXPECT_EQ(111, item9.mValue.int_value);

    const FieldValue& item10 = event1.getValues()[10];
    EXPECT_EQ(0x2020584, item10.mField.getField());
    EXPECT_EQ(Type::FLOAT, item10.mValue.getType());
    EXPECT_EQ(2.2f, item10.mValue.float_value);

    const FieldValue& item11 = event1.getValues()[11];
    EXPECT_EQ(0x2028601, item11.mField.getField());
    EXPECT_EQ(Type::INT, item11.mValue.getType());
    EXPECT_EQ(222, item11.mValue.int_value);

    const FieldValue& item12 = event1.getValues()[12];
    EXPECT_EQ(0x2028684, item12.mField.getField());
    EXPECT_EQ(Type::FLOAT, item12.mValue.getType());
    EXPECT_EQ(1.1f, item12.mValue.float_value);
}


}  // namespace statsd
}  // namespace os
}  // namespace android
#else
GTEST_LOG_(INFO) << "This test does nothing.\n";
#endif