use sha2::{Digest, Sha256};

pub fn hash_password(password: &str, salt: &str) -> String {
    let mut hasher = Sha256::new();
    hasher.update(format!("{salt}:{password}").as_bytes());
    hex::encode(hasher.finalize())
}
