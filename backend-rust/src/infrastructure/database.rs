use crate::config::MysqlSettings;
use rbatis::RBatis;
use rbdc_mysql::driver::MysqlDriver;

pub async fn connect_mysql(settings: &MysqlSettings) -> Result<RBatis, Box<dyn std::error::Error>> {
    let rb = RBatis::new();
    rb.init(MysqlDriver {}, &settings.url())?;
    Ok(rb)
}
