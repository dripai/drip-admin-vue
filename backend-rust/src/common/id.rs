use serde::{Deserialize, Deserializer, Serialize, Serializer};
use std::fmt;
use std::sync::atomic::{AtomicU16, Ordering};
use std::time::{SystemTime, UNIX_EPOCH};

static SEQUENCE: AtomicU16 = AtomicU16::new(0);
const CUSTOM_EPOCH_MILLIS: u64 = 1_735_689_600_000;

#[derive(Debug, Clone, Copy, Default, PartialEq, Eq, PartialOrd, Ord, Hash)]
pub struct I64String(pub i64);

impl I64String {
    pub fn value(self) -> i64 {
        self.0
    }
}

impl From<i64> for I64String {
    fn from(value: i64) -> Self {
        Self(value)
    }
}

impl fmt::Display for I64String {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "{}", self.0)
    }
}

impl Serialize for I64String {
    fn serialize<S>(&self, serializer: S) -> Result<S::Ok, S::Error>
    where
        S: Serializer,
    {
        serializer.serialize_str(&self.0.to_string())
    }
}

impl<'de> Deserialize<'de> for I64String {
    fn deserialize<D>(deserializer: D) -> Result<Self, D::Error>
    where
        D: Deserializer<'de>,
    {
        struct Visitor;

        impl<'de> serde::de::Visitor<'de> for Visitor {
            type Value = I64String;

            fn expecting(&self, formatter: &mut fmt::Formatter) -> fmt::Result {
                formatter.write_str("a string or integer i64 value")
            }

            fn visit_i64<E>(self, value: i64) -> Result<Self::Value, E> {
                Ok(I64String(value))
            }

            fn visit_u64<E>(self, value: u64) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                i64::try_from(value)
                    .map(I64String)
                    .map_err(|_| E::custom("u64 is too large for i64"))
            }

            fn visit_str<E>(self, value: &str) -> Result<Self::Value, E>
            where
                E: serde::de::Error,
            {
                if value.trim().is_empty() {
                    return Ok(I64String(0));
                }
                value
                    .parse::<i64>()
                    .map(I64String)
                    .map_err(|err| E::custom(err.to_string()))
            }
        }

        deserializer.deserialize_any(Visitor)
    }
}

pub fn next_id() -> i64 {
    let now_millis = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .expect("system clock must be after UNIX epoch")
        .as_millis() as u64;
    let timestamp = now_millis.saturating_sub(CUSTOM_EPOCH_MILLIS);
    let sequence = (SEQUENCE.fetch_add(1, Ordering::Relaxed) & 0x0fff) as u64;
    ((timestamp << 12) | sequence) as i64
}
